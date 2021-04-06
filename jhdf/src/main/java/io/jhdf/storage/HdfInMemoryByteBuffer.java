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

import io.jhdf.Superblock;
import io.jhdf.exceptions.InMemoryHdfException;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class HdfInMemoryByteBuffer implements HdfBackingStorage {

	private final ByteBuffer byteBuffer;
	private final Superblock superblock;

	public HdfInMemoryByteBuffer(ByteBuffer byteBuffer, Superblock superblock) {
		this.byteBuffer = byteBuffer.asReadOnlyBuffer();
		this.superblock = superblock;
	}

	@Override
	public ByteBuffer readBufferFromAddress(long address, int length) {
		return map(address, length);
	}

	@Override
	public ByteBuffer map(long address, long length) {
		return mapNoOffset(address + superblock.getBaseAddressByte(), length);
	}

	@Override
	public synchronized ByteBuffer mapNoOffset(long address, long length) {
		byteBuffer.limit(Math.toIntExact(address + length));
		byteBuffer.position(Math.toIntExact(address));
		// Set order on sliced buffer always LE
		return byteBuffer.slice().order(LITTLE_ENDIAN);
	}

	@Override
	public long getUserBlockSize() {
		return superblock.getBaseAddressByte();
	}

	@Override
	public Superblock getSuperblock() {
		return superblock;
	}

	@Override
	public FileChannel getFileChannel() {
		throw new InMemoryHdfException();
	}

	@Override
	public int getSizeOfOffsets() {
		return superblock.getSizeOfOffsets();
	}

	@Override
	public int getSizeOfLengths() {
		return superblock.getSizeOfLengths();
	}

	@Override
	public void close() {
		// NO-OP
	}

	@Override
	public long size() {
		return byteBuffer.capacity();
	}

	@Override
	public boolean inMemory() {
		return true;
	}
}
