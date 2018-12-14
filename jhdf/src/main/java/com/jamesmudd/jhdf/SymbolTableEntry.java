package com.jamesmudd.jhdf;

import static com.jamesmudd.jhdf.Utils.toHex;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class SymbolTableEntry {
	private static final Logger logger = LoggerFactory.getLogger(SymbolTableEntry.class);

	/** The location of this symbol table entry in the file */
	private final long address;
	private final int linkNameOffset;
	private final long objectHeaderAddress;
	private final int cacheType;
	private long bTreeAddress = -1;
	private long nameHeapAddress = -1;
	private long linkValueOffset = -1;

	public SymbolTableEntry(FileChannel fc, long address, Superblock sb) {
		this.address = address;

		int size = sb.getSizeOfOffsets() * 2 + 4 + 4 + 16;
		ByteBuffer bb = ByteBuffer.allocate(size);

		try {
			fc.read(bb, address);
		} catch (IOException e) {
			throw new HdfException("Failed to read file at address: " + Utils.toHex(address), e);
		}
		bb.rewind();
		bb.order(LITTLE_ENDIAN);

		// Link Name Offset
		linkNameOffset = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfOffsets());
		logger.trace("linkNameOffset = {}", linkNameOffset);

		// Object Header Address
		objectHeaderAddress = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfOffsets());
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
			bTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			logger.trace("addressOfBTree = {}", bTreeAddress);

			// Address of Name Heap
			nameHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			logger.trace("nameHeapAddress = {}", nameHeapAddress);
			break;
		case 2:
			// Link
			linkValueOffset = Utils.readBytesAsUnsignedInt(bb, 4);
			logger.trace("linkValueoffset = {}", linkValueOffset);
			break;
		default:
			throw new IllegalStateException("SymbolTableEntry: Unreconized cache type = " + cacheType);
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

	public long getLinkValueOffset() {
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
