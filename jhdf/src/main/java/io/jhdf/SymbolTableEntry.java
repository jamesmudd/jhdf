/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static io.jhdf.Utils.toHex;

public class SymbolTableEntry {
	private static final Logger logger = LoggerFactory.getLogger(SymbolTableEntry.class);

	/** The location of this symbol table entry in the file */
	private final long address;
	private final int linkNameOffset;
	private final long objectHeaderAddress;
	private final int cacheType;
	private long bTreeAddress = -1;
	private long nameHeapAddress = -1;
	private int linkValueOffset = -1;

	public SymbolTableEntry(HdfFileChannel fc, long address) {
		this.address = address;

		final int size = fc.getSizeOfOffsets() * 2 + 4 + 4 + 16;

		final ByteBuffer bb = fc.readBufferFromAddress(address, size);

		// Link Name Offset
		linkNameOffset = Utils.readBytesAsUnsignedInt(bb, fc.getSizeOfOffsets());
		logger.trace("linkNameOffset = {}", linkNameOffset);

		// Object Header Address
		objectHeaderAddress = Utils.readBytesAsUnsignedLong(bb, fc.getSizeOfOffsets());
		logger.trace("objectHeaderAddress = {}", objectHeaderAddress);

		// Link Name Offset
		cacheType = Utils.readBytesAsUnsignedInt(bb, 4);
		logger.trace("cacheType = {}", cacheType);

		// Reserved 4 bytes
		bb.get(new byte[4]);

		// Scratch pad
		switch (cacheType) {
		case 0:
			// Nothing in scratch pad space
			break;
		case 1:
			// B Tree
			// Address of B Tree
			bTreeAddress = Utils.readBytesAsUnsignedLong(bb, fc.getSizeOfOffsets());
			logger.trace("addressOfBTree = {}", bTreeAddress);

			// Address of Name Heap
			nameHeapAddress = Utils.readBytesAsUnsignedLong(bb, fc.getSizeOfOffsets());
			logger.trace("nameHeapAddress = {}", nameHeapAddress);
			break;
		case 2:
			// Link
			linkValueOffset = Utils.readBytesAsUnsignedInt(bb, 4);
			logger.trace("linkValueOffset = {}", linkValueOffset);
			break;
		default:
			throw new IllegalStateException("SymbolTableEntry: Unrecognized cache type = " + cacheType);
		}

	}

	public long getAddress() {
		return address;
	}

	public long getBTreeAddress() {
		return bTreeAddress;
	}

	public int getCacheType() {
		return cacheType;
	}

	public int getLinkNameOffset() {
		return linkNameOffset;
	}

	public int getLinkValueOffset() {
		return linkValueOffset;
	}

	public long getNameHeapAddress() {
		return nameHeapAddress;
	}

	public long getObjectHeaderAddress() {
		return objectHeaderAddress;
	}

	@Override
	public String toString() {
		return "SymbolTableEntry [address=" + toHex(address) + ", linkNameOffset=" + linkNameOffset
				+ ", objectHeaderAddress=" + toHex(objectHeaderAddress) + ", cacheType=" + cacheType + ", bTreeAddress="
				+ toHex(bTreeAddress) + ", nameHeapAddress=" + toHex(nameHeapAddress) + ", linkValueOffset="
				+ linkValueOffset + "]";
	}

}
