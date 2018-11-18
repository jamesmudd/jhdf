package com.jamesmudd.jhdf;

import static com.jamesmudd.jhdf.Utils.toHex;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SymbolTableEntry {
	private static final Logger logger = LoggerFactory.getLogger(SymbolTableEntry.class);

	/** The location of this symbol table entry in the file */
	private final long address;
	private final long linkNameOffset;
	private final long objectHeaderAddress;
	private final int cacheType;
	private long bTreeAddress = -1;
	private long nameHeapAddress = -1;
	private long linkValueoffset = -1;

	public SymbolTableEntry(RandomAccessFile file, long address, int sizeOfOffsets) throws IOException {
		this.address = address;
		file.seek(address);

		final byte[] offsetBytes = new byte[sizeOfOffsets];

		// Link Name Offset
		file.read(offsetBytes);
		linkNameOffset = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
		logger.trace("linkNameOffset = {}", linkNameOffset);

		// Object Header Address
		file.read(offsetBytes);
		objectHeaderAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
		logger.trace("objectHeaderAddress = {}", objectHeaderAddress);

		final byte[] fourBytes = new byte[4];

		// Link Name Offset
		file.read(fourBytes);
		cacheType = ByteBuffer.wrap(fourBytes).order(LITTLE_ENDIAN).getInt();
		logger.trace("cacheType = {}", cacheType);

		// Reserved 4 bytes
		file.skipBytes(4);

		// Scratch pad
		switch (cacheType) {
		case 0:
			// Nothing in scratch pad space
			break;
		case 1:
			// B Tree
			// Address of B Tree
			file.read(offsetBytes);
			bTreeAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("addressOfBTree = {}", bTreeAddress);

			// Address of Name Heap
			file.read(offsetBytes);
			nameHeapAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			logger.trace("nameHeapAddress = {}", nameHeapAddress);
			break;
		case 2:
			// Link
			file.read(fourBytes);
			linkValueoffset = ByteBuffer.wrap(fourBytes).order(LITTLE_ENDIAN).getInt();
			logger.trace("linkValueoffset = {}", linkValueoffset);
			break;
		default:
			throw new IllegalStateException("SymbolTableEntry: Unreconized cache type = " + cacheType);
		}

	}

	public long getbTreeAddress() {
		return bTreeAddress;
	}

	public int getCacheType() {
		return cacheType;
	}

	public long getLinkNameOffset() {
		return linkNameOffset;
	}

	public long getLinkValueoffset() {
		return linkValueoffset;
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
				+ toHex(bTreeAddress) + ", nameHeapAddress=" + toHex(nameHeapAddress) + ", linkValueoffset="
				+ linkValueoffset + "]";
	}

}
