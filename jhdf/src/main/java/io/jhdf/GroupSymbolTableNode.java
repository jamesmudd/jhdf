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

import io.jhdf.exceptions.HdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class GroupSymbolTableNode {
	private static final Logger logger = LoggerFactory.getLogger(GroupSymbolTableNode.class);

	private static final byte[] NODE_SIGNATURE = "SNOD".getBytes();

	/** The location of this GroupSymbolTableNode in the file */
	private final long address;
	private final short version;
	private final short numberOfEntries;
	private final SymbolTableEntry[] symbolTableEntries;

	public GroupSymbolTableNode(HdfFileChannel hdfFc, long address) {
		this.address = address;
		try {
			int headerSize = 8;
			ByteBuffer header = hdfFc.readBufferFromAddress(address, headerSize);

			byte[] formatSignatureBytes = new byte[4];
			header.get(formatSignatureBytes, 0, formatSignatureBytes.length);

			// Verify signature
			if (!Arrays.equals(NODE_SIGNATURE, formatSignatureBytes)) {
				throw new HdfException("Group symbol table Node signature not matched");
			}

			// Version Number
			version = header.get();

			// Move past reserved space
			header.position(6);

			final byte[] twoBytes = new byte[2];

			// Data Segment Size
			header.get(twoBytes);
			numberOfEntries = ByteBuffer.wrap(twoBytes).order(LITTLE_ENDIAN).getShort();
			logger.trace("numberOfSymbols = {}", numberOfEntries);

			final int symbolTableEntryBytes = hdfFc.getSizeOfOffsets() * 2 + 8 + 16;

			symbolTableEntries = new SymbolTableEntry[numberOfEntries];
			for (int i = 0; i < numberOfEntries; i++) {
				long offset = address + headerSize + i * symbolTableEntryBytes;
				symbolTableEntries[i] = new SymbolTableEntry(hdfFc, offset);
			}
		} catch (Exception e) {
			// TODO improve message
			throw new HdfException("Error reading Group symbol table node", e);
		}
	}

	public short getVersion() {
		return version;
	}

	public short getNumberOfEntries() {
		return numberOfEntries;
	}

	public SymbolTableEntry[] getSymbolTableEntries() {
		return symbolTableEntries;
	}

	@Override
	public String toString() {
		return "GroupSymbolTableNode [address=" + address + ", numberOfEntries=" + numberOfEntries + "]";
	}

}
