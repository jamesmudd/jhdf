/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps a {@link SeekableByteChannel} within a {@link FileChannel}
 */
public class FileChannelFromSeekableByteChannel extends FileChannel {
	private static final int MAX_TRANSFER_SIZE = 8192;
	private final SeekableByteChannel delegate;
	private final ReentrantLock lock = new ReentrantLock(true);

	public FileChannelFromSeekableByteChannel(SeekableByteChannel delegate) {
		this.delegate = delegate;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		lock.lock();
		try {
			return delegate.read(dst);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int read(ByteBuffer dst, long position) throws IOException {
		lock.lock();
		try {
			checkAccess(position);
			long originalPosition = delegate.position();
			try {
				delegate.position(position);
				return delegate.read(dst);
			} finally {
				delegate.position(originalPosition);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		lock.lock();
		try {
			int totalBytesRead = 0;
			for (int i = offset; i < offset + length; i++) {
				ByteBuffer dst = dsts[i];
				int bytesRead = read(dst);
				if (bytesRead == -1) {
					return totalBytesRead > 0 ? totalBytesRead : -1;
				}
				totalBytesRead += bytesRead;
				if (dst.hasRemaining()) {
					// For some reason the buffer has not been filled completely. This is a valid state in which we may return.
					break;
				}
			}
			return totalBytesRead;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		lock.lock();
		try {
			return delegate.write(src);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int write(ByteBuffer src, long position) throws IOException {
		lock.lock();
		try {
			checkAccess(position);
			long originalPosition = delegate.position();
			try {
				delegate.position(position);
				return delegate.write(src);
			} finally {
				delegate.position(originalPosition);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		lock.lock();
		try {
			int totalBytesWritten = 0;
			for (int i = offset; i < offset + length; i++) {
				ByteBuffer src = srcs[i];
				int bytesWritten = write(src);
				totalBytesWritten += bytesWritten;
				if (src.hasRemaining()) {
					// For some reason the buffer has not been written completely. This is a valid state in which we may return.
					break;
				}
			}
			return totalBytesWritten;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long position() throws IOException {
		lock.lock();
		try {
			return delegate.position();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public FileChannel position(long newPosition) throws IOException {
		lock.lock();
		try {
			delegate.position(newPosition);
			return this;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long size() throws IOException {
		lock.lock();
		try {
			return delegate.size();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public FileChannel truncate(long size) throws IOException {
		lock.lock();
		try {
			delegate.truncate(size);
			return this;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void force(boolean metaData) {
		throw new UnsupportedOperationException("Cannot force updates to the underlying file");
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
		lock.lock();
		try {
			checkTransfer(position, count, target);
			long size = size();
			if (count == 0 || position >= size) {
				// nothing to do
				return 0;
			}
			if (position + count > size) {
				// don't transfer more bytes than available
				count = size - position;
			}
			return transferData(delegate, target, position, count);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
		lock.lock();
		try {
			checkTransfer(position, count, src);
			long size = size();
			if (count == 0 || position > size) {
				// nothing to do
				return 0;
			}
			return transferData(src, delegate, position, count);
		} finally {
			lock.unlock();
		}
	}

	private void checkTransfer(long position, long count, Channel other) throws ClosedChannelException {
		lock.lock();
		try {
			if (!other.isOpen()) {
				throw new ClosedChannelException();
			}
			checkAccess(position);
			if (count < 0) {
				throw new IllegalArgumentException("Count must be non-negative");
			}
		} finally {
			lock.unlock();
		}
	}

	private void checkAccess(long position) throws ClosedChannelException {
		lock.lock();
		try {
			if (!isOpen()) {
				throw new ClosedChannelException();
			}
			if (position < 0) {
				throw new IllegalArgumentException("Position must be non-negative");
			}
		} finally {
			lock.unlock();
		}
	}

	private long transferData(ReadableByteChannel src, WritableByteChannel target, long delegateStartPosition, long count) throws IOException {
		lock.lock();
		try {
			long originalPosition = delegate.position();
			long totalBytesTransferred = 0;
			try {
				int capacity = (int) Math.min(count, MAX_TRANSFER_SIZE);
				ByteBuffer buffer = ByteBuffer.allocate(capacity);
				delegate.position(delegateStartPosition);
				while (totalBytesTransferred < count) {
					buffer.limit((int) Math.min(count - totalBytesTransferred, MAX_TRANSFER_SIZE));
					int bytesRead = src.read(buffer);
					if (bytesRead <= 0) {
						break;
					}
					buffer.flip();
					int bytesWritten = target.write(buffer);
					totalBytesTransferred += bytesWritten;

					if (bytesWritten != bytesRead) {
						/*
						 * We have read more bytes from src than written to target. We must adjust the position
						 * of src accordingly such that the read, but unwritten bytes are not lost forever.
						 * If src is the delegate, then its position will be reset anyway.
						 */
						if (src != delegate) {
							long readButUnwrittenBytes = bytesRead - bytesWritten;
							if (src instanceof SeekableByteChannel) {
								SeekableByteChannel srcChannel = (SeekableByteChannel) src;
								srcChannel.position(srcChannel.position() - readButUnwrittenBytes);
							} else {
								/*
								 * We can't adjust the position of src. Hence, we must force
								 * writing the unwritten bytes.
								 */
								while (bytesWritten < bytesRead) {
									int missingBytesWritten = target.write(buffer);
									if (missingBytesWritten == 0) {
										// avoid an infinite loop
										throw new IOException("Failed to write bytes to position " + delegate.position());
									}
									bytesWritten += missingBytesWritten;
									totalBytesTransferred += missingBytesWritten;
								}
							}
						}
						break;
					}
					buffer.clear();
				}
			} catch (IOException e) {
				if (totalBytesTransferred == 0) {
					throw e;
				}
			} finally {
				delegate.position(originalPosition);
			}
			return totalBytesTransferred;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileLock lock(long position, long size, boolean shared) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileLock tryLock(long position, long size, boolean shared) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void implCloseChannel() throws IOException {
		lock.lock();
		try {
			delegate.close();
		} finally {
			lock.unlock();
		}
	}
}
