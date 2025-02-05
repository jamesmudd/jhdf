package io.jhdf.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;

public class FileChannelFromSeekableByteChannel extends FileChannel
{
	private static final int MAX_TRANSFER_SIZE = 8192;
	private final SeekableByteChannel	delegate;

	public FileChannelFromSeekableByteChannel(SeekableByteChannel delegate) {
		this.delegate = delegate;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return delegate.read(dst);
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		int totalBytesRead = 0;
		for (int i = offset; i < offset + length; i++) {
			ByteBuffer dst = dsts[i];
			int bytesRead = read(dst);
			if (bytesRead == -1) {
				break;
			}
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return delegate.write(src);
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		int totalBytesWritten = 0;
		for (int i = offset; i < offset + length; i++) {
			ByteBuffer src = srcs[i];
			int bytesWritten = write(src);
			totalBytesWritten += bytesWritten;
		}
		return totalBytesWritten;
	}

	@Override
	public long position() throws IOException {
		return delegate.position();
	}

	@Override
	public FileChannel position(long newPosition) throws IOException {
		delegate.position(newPosition);
		return this;
	}

	@Override
	public long size() throws IOException {
		return delegate.size();
	}

	@Override
	public FileChannel truncate(long size) throws IOException {
		delegate.truncate(size);
		return this;
	}

	@Override
	public void force(boolean metaData) {
		throw new UnsupportedOperationException("Cannot force updates to the underlying file");
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
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
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
		checkTransfer(position, count, src);
		long size = size();
		if (count == 0 || position > size) {
			// nothing to do
			return 0;
		}
		return transferData(src, delegate, position, count);
	}

	private void checkTransfer(long position, long count, Channel other) throws ClosedChannelException {
		if (!other.isOpen()) {
			throw new ClosedChannelException();
		}
		checkAccess(position);
		if (count < 0) {
			throw new IllegalArgumentException("Count must be non-negative");
		}
	}

	private void checkAccess(long position) throws ClosedChannelException {
		if (!isOpen()) {
			throw new ClosedChannelException();
		}
		if (position < 0) {
			throw new IllegalArgumentException("Position must be non-negative");
		}
	}

	private long transferData(ReadableByteChannel from, WritableByteChannel to, long delegateStartPosition, long count) throws IOException {
		long originalPosition = delegate.position();
		long totalBytesTransferred = 0;
		try {
			int capacity = (int) Math.min(count, MAX_TRANSFER_SIZE);
			ByteBuffer buffer = ByteBuffer.allocate(capacity);
			delegate.position(delegateStartPosition);
			while (totalBytesTransferred < count) {
				buffer.limit((int) Math.min(count - totalBytesTransferred, MAX_TRANSFER_SIZE));
				int bytesRead = from.read(buffer);
				if (bytesRead <= 0) {
					break;
				}
				buffer.flip();
				int bytesWritten = to.write(buffer);
				totalBytesTransferred += bytesWritten;
				if (bytesWritten != bytesRead) {
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
	}

	@Override
	public int read(ByteBuffer dst, long position) throws IOException {
		checkAccess(position);
		long originalPosition = delegate.position();
		try {
			delegate.position(position);
			return delegate.read(dst);
		} finally {
			delegate.position(originalPosition);
		}
	}

	@Override
	public int write(ByteBuffer src, long position) throws IOException {
		checkAccess(position);
		long originalPosition = delegate.position();
		try {
			delegate.position(position);
			return delegate.write(src);
		} finally {
			delegate.position(originalPosition);
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
		delegate.close();
	}
}
