package io.jhdf.storage;

import io.jhdf.Superblock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class HdfInMemoryByteBuffer implements HdfBackingStorage {

	private final ByteBuffer byteBuffer;
	private final ByteOrder byteOrder;
	private final Superblock superblock;

	public HdfInMemoryByteBuffer(ByteBuffer byteBuffer, Superblock superblock) {
		this.byteBuffer = byteBuffer.asReadOnlyBuffer();
		this.byteOrder = byteBuffer.order();
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
	public ByteBuffer mapNoOffset(long address, long length) {
		byteBuffer.position(Math.toIntExact(address));
		byteBuffer.limit(Math.toIntExact(address + length));
		// Set order on sliced buffer
		return byteBuffer.slice().order(byteOrder);
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
	public FileChannel getFileChannel(){
		return null; //TODO
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
}
