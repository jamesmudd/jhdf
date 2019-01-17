package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

class BTreeNodeV2 extends BTreeNode {

		/** Type of node. 0 = group, 1 = data */
		private final short nodeType;

		/** Level of the node 0 = leaf */
//		private final short nodeLevel;
//		private final int entriesUsed;
//		private final long leftSiblingAddress;
//		private final long rightSiblingAddress;
//		private final long[] keys;
//		private final long[] childAddresses;

		BTreeNodeV2(FileChannel fc, Superblock sb, long address) {
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