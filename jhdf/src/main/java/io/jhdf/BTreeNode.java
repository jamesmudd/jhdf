package io.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

/**
 * B Tree nodes
 * 
 * @author James Mudd
 */
public abstract class BTreeNode {
	private static final Logger logger = LoggerFactory.getLogger(BTreeNode.class);

	private static final byte[] BTREE_NODE_V1_SIGNATURE = "TREE".getBytes();
	private static final byte[] BTREE_NODE_V2_SIGNATURE = "BTHD".getBytes();

	/** The location of this B tree in the file */
	private final long address;

	public BTreeNode(long address) {
		this.address = address;
	}

	private static class BTreeNodeV1 extends BTreeNode {

		/** Type of node. 0 = group, 1 = data */
		private final short nodeType;
		/** Level of the node 0 = leaf */
		private final short nodeLevel;
		private final int entriesUsed;
		private final long leftSiblingAddress;
		private final long rightSiblingAddress;
		private final long[] keys;
		private final long[] childAddresses;

		private BTreeNodeV1(FileChannel fc, Superblock sb, long address) {
			super(address);
			try {
				// B Tree Node Header
				// Something a little strange here this should be 4 + 2*sbOffsers but that
				// doesn't
				// work?
				int headerSize = 8 + 2 * sb.getSizeOfOffsets();
				ByteBuffer header = ByteBuffer.allocate(headerSize);
				fc.read(header, address + 4); // Skip signature already checked
				header.order(LITTLE_ENDIAN);
				header.rewind();

				nodeType = header.get();
				nodeLevel = header.get();

				entriesUsed = Utils.readBytesAsUnsignedInt(header, 2);
				logger.trace("Entries = {}", getEntriesUsed());

				leftSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
				logger.trace("left address = {}", getLeftSiblingAddress());

				rightSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
				logger.trace("right address = {}", getRightSiblingAddress());

				switch (nodeType) {
				case 0: // Group nodes
					int keyBytes = (2 * entriesUsed + 1) * sb.getSizeOfLengths();
					int childPointerBytes = (2 * entriesUsed) * sb.getSizeOfOffsets();
					int keysAndPointersBytes = keyBytes + childPointerBytes;

					ByteBuffer keysAndPointersBuffer = ByteBuffer.allocate(keysAndPointersBytes);
					fc.read(keysAndPointersBuffer, address + headerSize);
					keysAndPointersBuffer.order(LITTLE_ENDIAN);
					keysAndPointersBuffer.rewind();

					keys = new long[entriesUsed + 1];
					childAddresses = new long[entriesUsed];

					for (int i = 0; i < entriesUsed; i++) {
						keys[i] = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfLengths());
						childAddresses[i] = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfOffsets());
					}
					getKeys()[entriesUsed] = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer,
							sb.getSizeOfLengths());

					break;
				case 1: // Raw data
					// TODO implement
					throw new UnsupportedHdfException("B tree Raw data not implemented");
				default:
					throw new HdfException("Unreconized node type = " + nodeType);
				}

			} catch (IOException e) {
				throw new HdfException("Error reading B Tree node", e);
			}

		}

		public short getNodeType() {
			return nodeType;
		}

		@Override
		public short getNodeLevel() {
			return nodeLevel;
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

		public long[] getKeys() {
			return keys;
		}

		@Override
		public long[] getChildAddresses() {
			return childAddresses;
		}

		@Override
		public String toString() {
			return "BTreeNodeV1 [address=" + getAddress() + ", nodeType=" + nodeType + ", nodeLevel=" + nodeLevel + "]";
		}
	}

	private static class BTreeNodeV2 extends BTreeNode {

		/** Type of node. 0 = group, 1 = data */
		private final short nodeType;

		/** Level of the node 0 = leaf */
//		private final short nodeLevel;
//		private final int entriesUsed;
//		private final long leftSiblingAddress;
//		private final long rightSiblingAddress;
//		private final long[] keys;
//		private final long[] childAddresses;

		private BTreeNodeV2(FileChannel fc, Superblock sb, long address) {
			super(address);
			try {
				// B Tree V2 Header
				// Something a little strange here this should be 4 + 2*sbOffsers but that
				// doesn't
				// work?
				int headerSize = 12 + sb.getSizeOfOffsets() + 2 + sb.getSizeOfLengths() + 4;
				ByteBuffer bb = ByteBuffer.allocate(headerSize);
				fc.read(bb, address + 4); // Skip signature already checked
				bb.order(LITTLE_ENDIAN);
				bb.rewind();

				final byte version = bb.get();
				if (version != 0) {
					throw new HdfException("Unsupported B tree v2 version detected. Version; " + version);
				}

				nodeType = bb.get();
				final long nodeSize = Utils.readBytesAsUnsignedLong(bb, 4);
				final int recordSize = Utils.readBytesAsUnsignedInt(bb, 2);
				final int depth = Utils.readBytesAsUnsignedInt(bb, 2);

				final int splitPercent = Utils.readBytesAsUnsignedInt(bb, 1);
				final int mergePercent = Utils.readBytesAsUnsignedInt(bb, 1);

				final long rootNodeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

				final int numberOfRecordsInRoot = Utils.readBytesAsUnsignedInt(bb, 2);
				final int totalNumberOfRecordsInTree = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfLengths());

				final long checksum = Utils.readBytesAsUnsignedLong(bb, 4);

			} catch (IOException e) {
				throw new HdfException("Error reading B Tree node", e);
			}

		}

		public short getNodeType() {
			return nodeType;
		}

		@Override
		public String toString() {
			return "BTreeNodeV2 [address=" + getAddress() + ", nodeType=" + nodeType + "]";
		}

		@Override
		public long[] getChildAddresses() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public short getNodeLevel() {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	public static BTreeNode createBTreeNode(FileChannel fc, Superblock sb, long address) {

		ByteBuffer signatureBuffer = ByteBuffer.allocate(4);

		try {
			fc.read(signatureBuffer, address);
		} catch (IOException e) {
			throw new HdfException("Failed to read B Tree signature", e);
		}

		byte[] formatSignitureByte = new byte[4];
		signatureBuffer.rewind();
		signatureBuffer.get(formatSignitureByte, 0, formatSignitureByte.length);

		// Verify signature
		if (Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignitureByte)) {
			return new BTreeNodeV1(fc, sb, address);
		} else if (Arrays.equals(BTREE_NODE_V2_SIGNATURE, formatSignitureByte)) {
			return new BTreeNodeV2(fc, sb, address);
		} else {
			throw new HdfException("B tree node signature not matched");
		}
	}

	public abstract long[] getChildAddresses();

	public abstract short getNodeLevel();

	public long getAddress() {
		return address;
	}
}
