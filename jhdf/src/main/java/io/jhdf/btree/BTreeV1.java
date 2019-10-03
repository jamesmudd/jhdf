/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree;

import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Class to represent V1 B-trees
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#V1Btrees">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public abstract class BTreeV1 {
	private static final Logger logger = LoggerFactory.getLogger(BTreeV1.class);

	private static final byte[] BTREE_NODE_V1_SIGNATURE = "TREE".getBytes();
	private static final int HEADER_BYTES = 6;

	/** The location of this B tree in the file */
	private final long address;
	protected final int entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;

	public static BTreeV1Group createGroupBTree(HdfFileChannel hdfFc, long address) {
		ByteBuffer header = readHeaderAndValidateSignature(hdfFc, address);

		final byte nodeType = header.get();
		if (nodeType != 0) {
			throw new HdfException("B tree type is not group. Type is: " + nodeType);
		}

		final byte nodeLevel = header.get();

		if (nodeLevel > 0) {
			return new BTreeV1Group.BTreeV1GroupNonLeafNode(hdfFc, address);
		} else {
			return new BTreeV1Group.BTreeV1GroupLeafNode(hdfFc, address);
		}

	}

	public static BTreeV1Data createDataBTree(HdfFileChannel hdfFc, long address, int dataDimensions) {
		ByteBuffer header = readHeaderAndValidateSignature(hdfFc, address);

		final byte nodeType = header.get();
		if (nodeType != 1) {
			throw new HdfException("B tree type is not data. Type is: " + nodeType);
		}

		final byte nodeLevel = header.get();

		if (nodeLevel > 0) {
			return new BTreeV1Data.BTreeV1DataNonLeafNode(hdfFc, address, dataDimensions);
		} else {
			return new BTreeV1Data.BTreeV1DataLeafNode(hdfFc, address, dataDimensions);
		}
	}

	public static ByteBuffer readHeaderAndValidateSignature(HdfFileChannel fc, long address) {
		ByteBuffer header = fc.readBufferFromAddress(address, HEADER_BYTES);

		// Verify signature
		byte[] formatSignatureByte = new byte[4];
		header.get(formatSignatureByte, 0, formatSignatureByte.length);
		if (!Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignatureByte)) {
			throw new HdfException("B tree V1 node signature not matched");
		}
		return header;
	}

	/* package */ BTreeV1(HdfFileChannel hdfFc, long address) {
		this.address = address;

		int headerSize = 8 * hdfFc.getSizeOfOffsets();
		ByteBuffer header = hdfFc.readBufferFromAddress(address + 6, headerSize);

		entriesUsed = Utils.readBytesAsUnsignedInt(header, 2);
		logger.trace("Entries = {}", entriesUsed);

		leftSiblingAddress = Utils.readBytesAsUnsignedLong(header, hdfFc.getSizeOfOffsets());
		logger.trace("left address = {}", leftSiblingAddress);

		rightSiblingAddress = Utils.readBytesAsUnsignedLong(header, hdfFc.getSizeOfOffsets());
		logger.trace("right address = {}", rightSiblingAddress);

	}

	public int getEntriesUsed() {
		return entriesUsed;
	}

	public long getLeftSiblingAddress() {
		return leftSiblingAddress;
	}

	public long getRightSiblingAddress() {
		return rightSiblingAddress;
	}

	public long getAddress() {
		return address;
	}

	public abstract List<Long> getChildAddresses();

}
