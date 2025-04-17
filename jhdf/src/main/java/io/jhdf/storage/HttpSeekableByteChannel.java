/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link SeekableByteChannel} implementation that reads HDF5 file data over HTTP/HTTPS
 * using HTTP range requests.
 * <p>
 * This channel supports read-only access to remote files by downloading blocks of data on demand
 * and caching them in-memory. The cache is configurable by block size and block count, or by specifying
 * the total cache size in MB. The default configuration uses 128 KB blocks and a cache that holds up to
 * 16 MB (i.e. 128 blocks).
 * </p>
 * <p>
 * Throughout its usage, the channel tracks metrics such as total bytes read, number of blocks fetched,
 * cache hits, and cache misses. These metrics are logged at the INFO level upon channel closure.
 * </p>
 */
public class HttpSeekableByteChannel implements SeekableByteChannel {

	private static final Logger logger = LoggerFactory.getLogger(HttpSeekableByteChannel.class);

	// Default configuration constants.
	private static final int DEFAULT_BLOCK_SIZE = 128 * 1024; // 128 KB blocks.
	private static final int DEFAULT_CACHE_SIZE_MB = 16;      // 16 MB cache.
	private static final int DEFAULT_MAX_CACHE_BLOCKS = DEFAULT_CACHE_SIZE_MB * 1024 * 1024 / DEFAULT_BLOCK_SIZE;
	private static final int MAX_RETRIES = 3;

	// Instance configuration
	private final URL url;
	private final long size;
	private final int blockSize;
	private final LruCache<Long, byte[]> cache;
	private final ReentrantLock lock = new ReentrantLock();

	// Thread-safe position and metrics
	private long position = 0;
	private volatile boolean open = true;
	private long totalBytesRead = 0;
	private int totalBlocksFetched = 0;
	private int cacheHits = 0;
	private int cacheMisses = 0;

	/**
	 * Constructs a new HttpRangeSeekableByteChannel for the specified URL using the default cache configuration.
	 *
	 * @param url the URL of the remote HDF5 file.
	 * @throws IOException if an I/O error occurs while fetching the file size.
	 */
	public HttpSeekableByteChannel(URL url) throws IOException {
		this(url, DEFAULT_BLOCK_SIZE, DEFAULT_MAX_CACHE_BLOCKS);
	}

	/**
	 * Constructs a new HttpRangeSeekableByteChannel for the specified URL with custom block size and cache capacity.
	 *
	 * @param url            the URL of the remote HDF5 file.
	 * @param blockSize      the size of each cache block in bytes.
	 * @param maxCacheBlocks the maximum number of blocks to be stored in the cache.
	 * @throws IOException if an I/O error occurs while fetching the file size.
	 */
	public HttpSeekableByteChannel(URL url, int blockSize, int maxCacheBlocks) throws IOException {
		this.url = url;
		this.blockSize = blockSize;
		this.cache = new LruCache<>(maxCacheBlocks);
		this.size = fetchRemoteSize(url);
		logger.info("Initialized HttpRangeSeekableByteChannel for URL: {} with blockSize: {} bytes and cache capacity: {} blocks", url, blockSize, maxCacheBlocks);
	}

	/**
	 * Constructs a new HttpRangeSeekableByteChannel for the specified URL with a given total cache size in MB.
	 * The block size defaults to 128 KB.
	 *
	 * @param url         the URL of the remote HDF5 file.
	 * @param cacheSizeMB the total cache size in megabytes.
	 * @throws IOException if an I/O error occurs while fetching the file size.
	 */
	public HttpSeekableByteChannel(URL url, int cacheSizeMB) throws IOException {
		this(url, DEFAULT_BLOCK_SIZE, cacheSizeMB * 1024 * 1024 / DEFAULT_BLOCK_SIZE);
	}

	/**
	 * Retrieves the remote file size using a HEAD request.
	 *
	 * @param url the URL of the remote file.
	 * @return the file size in bytes.
	 * @throws IOException if an I/O error occurs during the request.
	 */
	private long fetchRemoteSize(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("HEAD");
		connection.connect();
		long len = connection.getContentLengthLong();
		connection.disconnect();
		logger.debug("Fetched remote file size: {} bytes", len);
		return len;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		lock.lock();
		try {
			int bytesRead = readImpl(dst, position);
			if (bytesRead > 0) {
				position += bytesRead;
				totalBytesRead += bytesRead;
			}
			logger.trace("Read {} bytes; updated position to {}", bytesRead, position);
			return bytesRead;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Implementation of the read operation without modifying the channel's position.
	 *
	 * @param dst          the destination ByteBuffer.
	 * @param readPosition the starting read position.
	 * @return the number of bytes read, or -1 if the end of file is reached.
	 * @throws IOException if an I/O error occurs during reading.
	 */
	private int readImpl(ByteBuffer dst, long readPosition) throws IOException {
		if (!open) throw new IOException("Channel is closed");
		if (readPosition >= size) return -1;

		int totalBytesReadLocal = 0;
		int bytesToRead = dst.remaining();

		while (bytesToRead > 0 && readPosition < size) {
			long blockIndex = readPosition / blockSize;
			int blockOffset = (int) (readPosition % blockSize);
			byte[] block = getOrFetchBlock(blockIndex);

			int bytesAvailable = Math.min(block.length - blockOffset, bytesToRead);
			dst.put(block, blockOffset, bytesAvailable);

			readPosition += bytesAvailable;
			bytesToRead -= bytesAvailable;
			totalBytesReadLocal += bytesAvailable;
		}
		return totalBytesReadLocal;
	}

	/**
	 * Retrieves a block from the cache or fetches it remotely if not present.
	 *
	 * @param blockIndex the index of the block to retrieve.
	 * @return the data block as a byte array.
	 * @throws IOException if an error occurs during remote fetching.
	 */
	private byte[] getOrFetchBlock(long blockIndex) throws IOException {
		try {
			int originalCacheHits = cacheHits;
			cacheHits++;
			return cache.computeIfAbsent(blockIndex, idx -> {
				try {
					byte[] block = fetchBlockFromRemote(idx);
					totalBlocksFetched++;
					cacheMisses++;
					cacheHits = originalCacheHits;
					return block;
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	/**
	 * Fetches a block of the remote file using HTTP range requests.
	 *
	 * @param blockIndex the index of the block to fetch.
	 * @return the data block as a byte array.
	 * @throws IOException if the block cannot be fetched after the maximum number of retries.
	 */
	private byte[] fetchBlockFromRemote(long blockIndex) throws IOException {
		long start = blockIndex * blockSize;
		long end = Math.min(size - 1, start + blockSize - 1);

		IOException lastException = null;
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
			connection.connect();

			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
				connection.disconnect();
				throw new IOException("Unexpected response code " + responseCode + " for range " + start + "-" + end);
			}

			try (InputStream in = connection.getInputStream()) {
				int expectedSize = (int) (end - start + 1);
				byte[] buffer = new byte[expectedSize];
				int totalRead = 0;
				while (totalRead < expectedSize) {
					int r = in.read(buffer, totalRead, expectedSize - totalRead);
					if (r == -1) break;
					totalRead += r;
				}
				if (totalRead != expectedSize) {
					throw new IOException("Incomplete read for block " + blockIndex + ": expected " + expectedSize + ", got " + totalRead);
				}
				logger.trace("Fetched block {} (bytes {}-{}) on attempt {}", blockIndex, start, end, attempt);
				return buffer;
			} catch (IOException e) {
				lastException = e;
				logger.debug("Attempt {} for block {} failed: {}", attempt, blockIndex, e.getMessage());
				try {
					Thread.sleep(100L * attempt);
				} catch (InterruptedException ignored) {
				}
			} finally {
				connection.disconnect();
			}
		}
		throw new IOException("Failed to fetch block " + blockIndex + " after " + MAX_RETRIES + " attempts", lastException);
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		lock.lock();
		try {
			if (newPosition < 0 || newPosition > size) {
				throw new IllegalArgumentException("Invalid position: " + newPosition);
			}
			position = newPosition;
			return this;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long position() {
		lock.lock();
		try {
			return position;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public SeekableByteChannel truncate(long size) {
		throw new UnsupportedOperationException("truncate not supported");
	}

	@Override
	public int write(ByteBuffer src) {
		throw new UnsupportedOperationException("write not supported");
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	/**
	 * Closes the channel and clears the cache. Logs usage metrics including total bytes read,
	 * blocks fetched, cache hits, and cache misses.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		lock.lock();
		try {
			open = false;
			cache.clear();
			logger.info("HttpSeekableByteChannel closed. Total bytes read: {}. Blocks fetched: {}. Cache hits: {}. Cache misses: {}.", totalBytesRead, totalBlocksFetched, cacheHits, cacheMisses);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * A simple Least Recently Used (LRU) cache implemented with {@link LinkedHashMap}.
	 *
	 * @param <K> the type of keys.
	 * @param <V> the type of values.
	 */
	private static class LruCache<K, V> extends LinkedHashMap<K, V> {
		private final int maxEntries;

		/**
		 * Constructs an LRU cache with the specified maximum number of entries.
		 *
		 * @param maxEntries the maximum number of entries the cache can hold.
		 */
		public LruCache(int maxEntries) {
			super(maxEntries, 0.75f, true);
			this.maxEntries = maxEntries;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > maxEntries;
		}
	}
}
