/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class GroupSymbolTableNode {
	private static final Logger logger = LoggerFactory.getLogger(GroupSymbolTableNode.class);

	private static final byte[] NODE_SIGNATURE = "SNOD".getBytes(StandardCharsets.US_ASCII);

	/** The location of this GroupSymbolTableNode in the file */
	private final long address;
	private final short version;
	private final short numberOfEntries;
	private final SymbolTableEntry[] symbolTableEntries;

	public GroupSymbolTableNode(HdfBackingStorage hdfBackingStorage, long address) {
		this.address = address;
		try {
			int headerSize = 8;
			ByteBuffer header = hdfBackingStorage.readBufferFromAddress(address, headerSize);

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

			final long symbolTableEntryBytes = hdfBackingStorage.getSizeOfOffsets() * 2L + 8L + 16L;

			symbolTableEntries = new SymbolTableEntry[numberOfEntries];
			for (int i = 0; i < numberOfEntries; i++) {
				long offset = address + headerSize + i * symbolTableEntryBytes;
				symbolTableEntries[i] = new SymbolTableEntry(hdfBackingStorage, offset);
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
		return ArrayUtils.clone(symbolTableEntries);
	}

	@Override
	public String toString() {
		return "GroupSymbolTableNode [address=" + address + ", numberOfEntries=" + numberOfEntries + "]";
	}

}
