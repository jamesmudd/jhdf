package io.jhdf.btree;

import static io.jhdf.Utils.readBytesAsUnsignedInt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class BTreeV2 {

	private static final int NODE_OVERHEAD_BYTES = 10;

	private static final byte[] BTREE_NODE_V2_SIGNATURE = "BTHD".getBytes();
	private static final byte[] BTREE_INTERNAL_NODE_SIGNATURE = "BTIN".getBytes();
	private static final byte[] BTREE_LEAF_NODE_SIGNATURE = "BTLF".getBytes();

	/** The location of this B tree in the file */
	private final long address;
	/** Type of node. */
	private final short nodeType;

	private final List<BTreeRecord> records;

	public List<BTreeRecord> getRecords() {
		return records;
	}

	public static BTreeV2 createBTree(HdfFileChannel hdfFc, long address) {
		return new BTreeV2(hdfFc, address);
	}

	private BTreeV2(HdfFileChannel hdfFc, long address) {
		this.address = address;
		try {
			// B Tree V2 Header
			int headerSize = 16 + hdfFc.getSizeOfOffsets() + 2 + hdfFc.getSizeOfLengths() + 4;
			ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

			// Verify signature
			byte[] formatSignitureByte = new byte[4];
			bb.get(formatSignitureByte, 0, formatSignitureByte.length);
			if (!Arrays.equals(BTREE_NODE_V2_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("B tree V1 node signature not matched");
			}

			final byte version = bb.get();
			if (version != 0) {
				throw new HdfException("Unsupported B tree v2 version detected. Version: " + version);
			}

			nodeType = bb.get();
			final int nodeSize = Utils.readBytesAsUnsignedInt(bb, 4);
			final int recordSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int depth = Utils.readBytesAsUnsignedInt(bb, 2);

			final int splitPercent = Utils.readBytesAsUnsignedInt(bb, 1);
			final int mergePercent = Utils.readBytesAsUnsignedInt(bb, 1);

			final long rootNodeAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());

			final int numberOfRecordsInRoot = Utils.readBytesAsUnsignedInt(bb, 2);
			final int totalNumberOfRecordsInTree = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

			final long checksum = Utils.readBytesAsUnsignedLong(bb, 4);

			records = new ArrayList<>(totalNumberOfRecordsInTree);

			readRecords(hdfFc, rootNodeAddress, nodeSize, recordSize, depth, numberOfRecordsInRoot,
					totalNumberOfRecordsInTree, records);

		} catch (HdfException e) {
			throw new HdfException("Error reading B Tree node", e);
		}

	}

	private void readRecords(HdfFileChannel hdfFc, long address, int nodeSize, int recordSize, int depth,
			int numberOfRecords, int totalRecords, List<BTreeRecord> records) {

		ByteBuffer bb = hdfFc.readBufferFromAddress(address, nodeSize);

		byte[] nodeSignitureBytes = new byte[4];
		bb.get(nodeSignitureBytes, 0, nodeSignitureBytes.length);

		final boolean leafNode;
		if (Arrays.equals(BTREE_INTERNAL_NODE_SIGNATURE, nodeSignitureBytes)) {
			leafNode = false;
		} else if (Arrays.equals(BTREE_LEAF_NODE_SIGNATURE, nodeSignitureBytes)) {
			leafNode = true;
		} else {
			throw new HdfException("B tree internal node signature not matched");
		}

		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unsupported B tree v2 internal node version detected. Version: " + version);
		}

		final byte type = bb.get();

		for (int i = 0; i < numberOfRecords; i++) {
			records.add(BTreeRecord.readRecord(type, Utils.createSubBuffer(bb, recordSize)));
		}

		if (!leafNode) {
			for (int i = 0; i < numberOfRecords + 1; i++) {
				final long childAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
				int sizeOfNumberOfRecords = getSizeOfNumberOfRecords(nodeSize, depth, totalRecords, recordSize,
						hdfFc.getSizeOfOffsets());
				final int numberOfChildRecords = readBytesAsUnsignedInt(bb, sizeOfNumberOfRecords);
				final int totalNumberOfChildRecords;
				if (depth > 1) {
					totalNumberOfChildRecords = readBytesAsUnsignedInt(bb,
							getSizeOfTotalNumberOfChildRecords(nodeSize, depth, recordSize));
				} else {
					totalNumberOfChildRecords = -1;
				}
				readRecords(hdfFc, childAddress, nodeSize, recordSize, depth - 1, numberOfChildRecords,
						totalNumberOfChildRecords, records);
			}
		}
		// TODO Checksum
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