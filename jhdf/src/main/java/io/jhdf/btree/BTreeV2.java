/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree;

import io.jhdf.Utils;
import io.jhdf.btree.record.BTreeRecord;
import io.jhdf.checksum.ChecksumUtils;
import io.jhdf.dataset.chunked.DatasetInfo;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.jhdf.Utils.createSubBuffer;
import static io.jhdf.Utils.readBytesAsUnsignedInt;
import static io.jhdf.Utils.readBytesAsUnsignedLong;
import static io.jhdf.btree.record.BTreeRecord.readRecord;

public class BTreeV2<T extends BTreeRecord> {

	private static final int NODE_OVERHEAD_BYTES = 10;

	private static final byte[] BTREE_NODE_V2_SIGNATURE = "BTHD".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] BTREE_INTERNAL_NODE_SIGNATURE = "BTIN".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] BTREE_LEAF_NODE_SIGNATURE = "BTLF".getBytes(StandardCharsets.US_ASCII);

	/**
	 * The location of this B tree in the file
	 */
	private final long address;
	/**
	 * Type of node.
	 */
	private final int nodeType;
	/**
	 * The records in this b-tree
	 */
	private final List<T> records;
	/**
	 * bytes in each node
	 */
	private final int nodeSize;
	/**
	 * bytes in each record
	 */
	private final int recordSize;

	public List<T> getRecords() {
		return records;
	}

	public BTreeV2(HdfBackingStorage hdfBackingStorage, long address) {
		this(hdfBackingStorage, address, null);
	}

	public BTreeV2(HdfBackingStorage hdfBackingStorage, long address, DatasetInfo datasetInfo) {
		this.address = address;
		try {
			// B Tree V2 Header
			int headerSize = 16 + hdfBackingStorage.getSizeOfOffsets() + 2 + hdfBackingStorage.getSizeOfLengths() + 4;
			ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, headerSize);

			// Verify signature
			byte[] formatSignatureBytes = new byte[4];
			bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);
			if (!Arrays.equals(BTREE_NODE_V2_SIGNATURE, formatSignatureBytes)) {
				throw new HdfException("B tree V2 node signature not matched");
			}

			final byte version = bb.get();
			if (version != 0) {
				throw new HdfException("Unsupported B tree v2 version detected. Version: " + version);
			}

			nodeType = bb.get();
			if ((nodeType == 10 || nodeType == 11) && datasetInfo == null) {
				throw new HdfException("datasetInfo not set when building chunk B tree");
			}

			nodeSize = Utils.readBytesAsUnsignedInt(bb, 4);
			recordSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int depth = Utils.readBytesAsUnsignedInt(bb, 2);

			final int splitPercent = Utils.readBytesAsUnsignedInt(bb, 1);
			final int mergePercent = Utils.readBytesAsUnsignedInt(bb, 1);

			final long rootNodeAddress = readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());

			final int numberOfRecordsInRoot = Utils.readBytesAsUnsignedInt(bb, 2);
			final int totalNumberOfRecordsInTree = Utils.readBytesAsUnsignedInt(bb, hdfBackingStorage.getSizeOfLengths());

			bb.rewind();
			ChecksumUtils.validateChecksum(bb);

			records = new ArrayList<>(totalNumberOfRecordsInTree);

			readRecords(hdfBackingStorage, rootNodeAddress, depth, numberOfRecordsInRoot, totalNumberOfRecordsInTree, datasetInfo);

		} catch (HdfException e) {
			throw new HdfException("Error reading B Tree node", e);
		}

	}

	private void readRecords(HdfBackingStorage hdfBackingStorage, long address, int depth, int numberOfRecords, int totalRecords, DatasetInfo datasetInfo) {

		ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, nodeSize);

		byte[] nodeSignatureBytes = new byte[4];
		bb.get(nodeSignatureBytes, 0, nodeSignatureBytes.length);

		final boolean leafNode;
		if (Arrays.equals(BTREE_INTERNAL_NODE_SIGNATURE, nodeSignatureBytes)) {
			leafNode = false;
		} else if (Arrays.equals(BTREE_LEAF_NODE_SIGNATURE, nodeSignatureBytes)) {
			leafNode = true;
		} else {
			throw new HdfException("B tree internal node signature not matched");
		}

		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unsupported B tree v2 internal node version detected. Version: " + version);
		}

		final int type = bb.get();

		for (int i = 0; i < numberOfRecords; i++) {
			records.add(readRecord(type, createSubBuffer(bb, recordSize), datasetInfo));
		}

		if (!leafNode) {
			for (int i = 0; i < numberOfRecords + 1; i++) {
				final long childAddress = readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());
				int sizeOfNumberOfRecords = getSizeOfNumberOfRecords(nodeSize, depth, totalRecords, recordSize,
					hdfBackingStorage.getSizeOfOffsets());
				final int numberOfChildRecords = readBytesAsUnsignedInt(bb, sizeOfNumberOfRecords);
				final int totalNumberOfChildRecords;
				if (depth > 1) {
					totalNumberOfChildRecords = readBytesAsUnsignedInt(bb,
						getSizeOfTotalNumberOfChildRecords(nodeSize, depth, recordSize));
				} else {
					totalNumberOfChildRecords = -1;
				}
				readRecords(hdfBackingStorage, childAddress, depth - 1, numberOfChildRecords, totalNumberOfChildRecords, datasetInfo);
			}
		}
		bb.limit(bb.position() + 4);
		bb.rewind();
		ChecksumUtils.validateChecksum(bb);
	}

	private int getSizeOfNumberOfRecords(int nodeSize, int depth, int totalRecords, int recordSize, int sizeOfOffsets) {
		int size = nodeSize - NODE_OVERHEAD_BYTES;

		// If the child is not a leaf
		if (depth > 1) {
			// Need to subtract the pointers as well
			int pointerTripletBytes = bytesNeededToHoldNumber(totalRecords) * 2 + sizeOfOffsets;
			size -= pointerTripletBytes;

			return bytesNeededToHoldNumber(size / recordSize);
		} else {
			// Its a leaf
			return bytesNeededToHoldNumber(size / recordSize);
		}
	}

	private int bytesNeededToHoldNumber(int number) {
		return (Integer.numberOfTrailingZeros(Integer.highestOneBit(number)) + 8) / 8;
	}

	private int getSizeOfTotalNumberOfChildRecords(int nodeSize, int depth, int recordSize) {
		int recordsInLeafNode = nodeSize / recordSize;
		return (BigInteger.valueOf(recordsInLeafNode).pow(depth).bitLength() + 8) / 8;
	}

	@Override
	public String toString() {
		return "BTreeNodeV2 [address=" + address + ", nodeType=" + nodeType + "]";
	}

}
