package io.jhdf.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class HttpRangeSeekableByteChannel implements SeekableByteChannel {

	private static final int BLOCK_SIZE = 64 * 1024; // 64 KB blocks
	private static final int MAX_CACHE_BLOCKS = 1024;  // ~64 MB total cache
	private static final int MAX_RETRIES = 3;

	private final URL url;
	private final long size;
	private final LruCache<Long, byte[]> cache = new LruCache<>(MAX_CACHE_BLOCKS);

	// Use a single lock for all operations.
	private final ReentrantLock lock = new ReentrantLock();

	// Shared file pointer.
	private long position = 0;

	private volatile boolean open = true;

	public HttpRangeSeekableByteChannel(URL url) throws IOException {
		this.url = url;
		this.size = fetchRemoteSize(url);
	}

	private long fetchRemoteSize(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("HEAD");
		connection.connect();
		long len = connection.getContentLengthLong();
		connection.disconnect();
		return len;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		lock.lock();
		try {
			int bytesRead = readImpl(dst, position);
			if (bytesRead > 0) {
				position += bytesRead;
			}
			return bytesRead;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Common implementation for read() that does not update shared state.
	 */
	private int readImpl(ByteBuffer dst, long readPosition) throws IOException {
		if (!open) throw new IOException("Channel is closed");
		if (readPosition >= size) return -1;

		int totalBytesRead = 0;
		int bytesToRead = dst.remaining();

		while (bytesToRead > 0 && readPosition < size) {
			long blockIndex = readPosition / BLOCK_SIZE;
			int blockOffset = (int) (readPosition % BLOCK_SIZE);
			byte[] block = getOrFetchBlock(blockIndex);

			int bytesAvailable = Math.min(block.length - blockOffset, bytesToRead);
			dst.put(block, blockOffset, bytesAvailable);

			readPosition += bytesAvailable;
			bytesToRead -= bytesAvailable;
			totalBytesRead += bytesAvailable;
		}
		return totalBytesRead;
	}

	/**
	 * Retrieves a block from the cache or fetches it remotely if missing.
	 */
	private byte[] getOrFetchBlock(long blockIndex) throws IOException {
		byte[] block = cache.get(blockIndex);
		if (block != null) {
			return block;
		}
		block = fetchBlockFromRemote(blockIndex);
		cache.put(blockIndex, block);
		return block;
	}

	/**
	 * Fetches a block of the remote file.
	 * Checks that the server returns HTTP_PARTIAL (206) and validates that the complete expected number
	 * of bytes is read. Retries are attempted on failures.
	 */
	private byte[] fetchBlockFromRemote(long blockIndex) throws IOException {
		long start = blockIndex * BLOCK_SIZE;
		long end = Math.min(size - 1, start + BLOCK_SIZE - 1);

		IOException lastException = null;
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
			connection.connect();

			// Check that the server returns Partial Content (206)
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
					throw new IOException("Incomplete read for block " + blockIndex + ": expected " +
						expectedSize + ", got " + totalRead);
				}
				return buffer;
			} catch (IOException e) {
				lastException = e;
				try {
					Thread.sleep(100L * attempt);
				} catch (InterruptedException ignored) {}
			} finally {
				connection.disconnect();
			}
		}
		throw new IOException("Failed to fetch block " + blockIndex + " after " + MAX_RETRIES +
			" attempts", lastException);
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		lock.lock();
		try {
			if (newPosition < 0 || newPosition > size)
				throw new IllegalArgumentException("Invalid position: " + newPosition);
			this.position = newPosition;
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

	@Override
	public void close() throws IOException {
		lock.lock();
		try {
			open = false;
			cache.clear();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * A simple LRU cache implemented via LinkedHashMap.
	 */
	private static class LruCache<K, V> extends LinkedHashMap<K, V> {
		private final int maxEntries;

		public LruCache(int maxEntries) {
			super(16, 0.75f, true);
			this.maxEntries = maxEntries;
		}
		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > maxEntries;
		}
	}
}
