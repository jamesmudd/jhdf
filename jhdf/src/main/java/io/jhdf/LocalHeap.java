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

public class LocalHeap {
	private static final Logger logger = LoggerFactory.getLogger(LocalHeap.class);

	private static final byte[] HEAP_SIGNATURE = "HEAP".getBytes();

	/** The location of this Heap in the file */
	private final long address;
	private final short version;
	private final long dataSegmentSize;
	private final long offsetToHeadOfFreeList;
	private final long addressOfDataSegment;
	private final ByteBuffer dataBuffer;

	public LocalHeap(HdfFileChannel hdfFc, long address) {
		this.address = address;
		try {
			// Header
			int headerSize = 8 + hdfFc.getSizeOfLengths() + hdfFc.getSizeOfLengths() + hdfFc.getSizeOfOffsets();
			ByteBuffer header = hdfFc.readBufferFromAddress(address, headerSize);

			byte[] formatSignatureBytes = new byte[4];
			header.get(formatSignatureBytes, 0, formatSignatureBytes.length);

			// Verify signature
			if (!Arrays.equals(HEAP_SIGNATURE, formatSignatureBytes)) {
				throw new HdfException("Heap signature not matched");
			}

			// Version
			version = header.get();

			// Move past reserved space
			header.position(8);

			// Data Segment Size
			dataSegmentSize = Utils.readBytesAsUnsignedLong(header, hdfFc.getSizeOfLengths());
			logger.trace("dataSegmentSize = {}", dataSegmentSize);

			// Offset to Head of Free-list
			offsetToHeadOfFreeList = Utils.readBytesAsUnsignedLong(header, hdfFc.getSizeOfLengths());
			logger.trace("offsetToHeadOfFreeList = {}", offsetToHeadOfFreeList);

			// Address of Data Segment
			addressOfDataSegment = Utils.readBytesAsUnsignedLong(header, hdfFc.getSizeOfOffsets());
			logger.trace("addressOfDataSegment = {}", addressOfDataSegment);

			dataBuffer = hdfFc.map(addressOfDataSegment, dataSegmentSize);
		} catch (Exception e) {
			throw new HdfException("Error reading local heap", e);
		}
	}

	public short getVersion() {
		return version;
	}

	public long getDataSegmentSize() {
		return dataSegmentSize;
	}

	public long getOffsetToHeadOfFreeList() {
		return offsetToHeadOfFreeList;
	}

	public long getAddressOfDataSegment() {
		return addressOfDataSegment;
	}

	@Override
	public String toString() {
		return "LocalHeap [address=" + Utils.toHex(address) + ", version=" + version + ", dataSegmentSize="
				+ dataSegmentSize + ", offsetToHeadOfFreeList=" + offsetToHeadOfFreeList + ", addressOfDataSegment="
				+ Utils.toHex(addressOfDataSegment) + "]";
	}

	public ByteBuffer getDataBuffer() {
		return dataBuffer;
	}

}
