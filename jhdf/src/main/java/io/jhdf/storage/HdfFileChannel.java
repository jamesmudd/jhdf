/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.storage;

import io.jhdf.HdfFile;
import io.jhdf.Superblock;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfWritingException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * This wraps a {@link FileChannel} and combines it with the HDF5
 * {@link Superblock}. It allows a single object to be passed around inside a
 * {@link HdfFile} and provides convenience methods for common operations.
 *
 * @author James Mudd
 */
public class HdfFileChannel implements HdfBackingStorage {

	private final FileChannel fc;
	private final Superblock sb;
	private boolean memoryMappingFailed;

	public HdfFileChannel(FileChannel fileChannel, Superblock superblock) {
		this.fc = fileChannel;
		this.sb = superblock;
	}

	/**
	 * Reads from the HDF file into a {@link ByteBuffer}. It takes in to account the
	 * 'base address' so the offset for user block is handled. It also converts the
	 * buffer to {@link ByteOrder#LITTLE_ENDIAN} and rewinds the buffer ready for
	 * use.
	 *
	 * @param address the address to read from
	 * @param length  the length of the buffer to read
	 * @return the buffer
	 * @throws HdfException if an error occurs during the read
	 */
	@Override
	public ByteBuffer readBufferFromAddress(long address, int length) {
		long rawAddress = address + sb.getBaseAddressByte();
		try {
			return readBufferNoOffset(rawAddress, length);
		} catch (IOException e) {
			throw new HdfException(
				"Failed to read from file at address '" + address + "' (raw address '" + rawAddress + "'", e);
		}
	}

	@Override
	public ByteBuffer map(long address, long length) {
		return mapNoOffset(address + sb.getBaseAddressByte(), length);
	}

	@Override
	public ByteBuffer mapNoOffset(long address, long length) {
		return mapNoOffset(address, length, MapMode.READ_ONLY);
	}

	public ByteBuffer mapNoOffset(long address, long length, MapMode mode) {
		try {
			if (!memoryMappingFailed) {
				try {
					return fc.map(mode, address, length);
				} catch (UnsupportedOperationException e) {
					// many file systems do not support memory mapping
					memoryMappingFailed = true;
				}
			}
			assert memoryMappingFailed;
			// read channel into buffer instead of mapping it to memory
			return readBufferNoOffset(address, Math.toIntExact(length));
		} catch (IOException e) {
			throw new HdfException("Failed to map buffer at address '" + address
				+ "' of length '" + length + "'", e);
		}
	}

	private ByteBuffer readBufferNoOffset(long address, int length) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(length);
		int totalNumberOfBytesRead = 0;
		while (totalNumberOfBytesRead < length) {
			int numberOfBytesRead = fc.read(bb, address);
			if (numberOfBytesRead <= 0) {
				throw new IOException("Trying to read more bytes than available");
			}
			totalNumberOfBytesRead += numberOfBytesRead;
		}
		bb.order(LITTLE_ENDIAN);
		bb.flip();
		return bb;
	}

	@Override
	public long getUserBlockSize() {
		return sb.getBaseAddressByte();
	}

	@Override
	public Superblock getSuperblock() {
		return sb;
	}

	@Override
	public FileChannel getFileChannel() {
		return fc;
	}

	@Override
	public int getSizeOfOffsets() {
		return sb.getSizeOfOffsets();
	}

	@Override
	public int getSizeOfLengths() {
		return sb.getSizeOfLengths();
	}

	@Override
	public final void close() {
		try {
			fc.close();
		} catch (IOException e) {
			throw new HdfException("Failed closing HDF5 file", e);
		}
	}

	@Override
	public long size() {
		try {
			return fc.size();
		} catch (IOException e) {
			throw new HdfException("Failed to get size of HDF5 file", e);
		}
	}

	@Override
	public boolean inMemory() {
		return false;
	}

	public int write(ByteBuffer buffer, long position) {
		// TODO check if writable
		try {
			return fc.write(buffer, position);
		} catch (IOException e) {
			throw new HdfWritingException("Exception writing at position: " + position, e);
		}
	}
	public int write(ByteBuffer buffer) {
		// TODO check if writable
		try {
			return fc.write(buffer);
		} catch (IOException e) {
			throw new HdfWritingException("Exception writing", e);
		}
	}

	public void position(long dataAddress) {
		try {
			fc.position(dataAddress);
		} catch (IOException e) {
			throw new HdfWritingException("Exception writing", e);
		}
	}
}
