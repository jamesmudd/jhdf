/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.storage;

import io.jhdf.HdfFile;
import io.jhdf.Superblock;
import io.jhdf.exceptions.HdfException;

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
		ByteBuffer bb = ByteBuffer.allocate(length);
		try {
			fc.read(bb, address + sb.getBaseAddressByte());
		} catch (IOException e) {
			throw new HdfException(
				"Failed to read from file at address '" + address + "' (raw address '" + address
					+ sb.getBaseAddressByte() + "'",
				e);
		}
		bb.order(LITTLE_ENDIAN);
		bb.rewind();
		return bb;
	}

	@Override
	public ByteBuffer map(long address, long length) {
		return mapNoOffset(address + sb.getBaseAddressByte(), length);
	}

	@Override
	public ByteBuffer mapNoOffset(long address, long length) {
		try {
			try {
			return fc.map(MapMode.READ_ONLY, address, length);
			} catch (UnsupportedOperationException e) {
				// many file systems do not support memory mapping
				int lengthAsInt = (int) length;
				if (lengthAsInt != length) {
					// length must be representable by an int such that we can allocate a ByteBuffer manually
					throw e;
				}
				ByteBuffer buffer = ByteBuffer.allocate(lengthAsInt);
				long oldPosition = fc.position();
				fc.position(address);
				try {
					int totalNumberOfBytesRead = 0;
					while (totalNumberOfBytesRead < lengthAsInt) {
						int numberOfBytesRead = fc.read(buffer);
						if (numberOfBytesRead < 0) {
							throw new IOException("Trying to read more bytes than available");
						}
						totalNumberOfBytesRead += numberOfBytesRead;
					}
				}
				finally {
					// reset file channel to old position
					fc.position(oldPosition);
				}
				buffer.flip();
				return buffer;
			}
		} catch (IOException e) {
			throw new HdfException("Failed to map buffer at address '" + address
				+ "' of length '" + length + "'", e);
		}
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
}
