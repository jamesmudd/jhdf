package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

class BTreeNodeV1 extends BTreeNode {

	/** Type of node. 0 = group, 1 = data */
	private final short nodeType;
	/** Level of the node 0 = leaf */
	private final short nodeLevel;
	private final int entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;
	private final long[] keys;
	private final long[] childAddresses;

	BTreeNodeV1(FileChannel fc, Superblock sb, long address) {
		super(address);
		try {
			// B Tree Node Header
			// Something a little strange here this should be 4 + 2*sbOffsers but that
			// doesn't work?
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
				getKeys()[entriesUsed] = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfLengths());

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