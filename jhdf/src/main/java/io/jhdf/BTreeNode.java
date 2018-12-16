package io.jhdf;

import static io.jhdf.Utils.toHex;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfException;

public class BTreeNode {
	private static final Logger logger = LoggerFactory.getLogger(BTreeNode.class);

	private static final byte[] BTREE_NODE_SIGNATURE = "TREE".getBytes();

	/** The location of this B tree in the file */
	private final long address;
	/** Type of node. 0 = group, 1 = data */
	private final short nodeType;
	/** Level of the node 0 = leaf */
	private final short nodeLevel;
	private final short entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;
	private final long[] keys;
	private final long[] childAddresses;

	public BTreeNode(FileChannel fc, long address, Superblock sb) {
		this.address = address;
		try {
			// B Tree Node Header
			int headerSize = 8 + 2 * sb.getSizeOfOffsets();
			ByteBuffer header = ByteBuffer.allocate(headerSize);
			fc.read(header, address);
			header.order(LITTLE_ENDIAN);
			header.rewind();

			byte[] formatSignitureByte = new byte[4];
			header.get(formatSignitureByte, 0, formatSignitureByte.length);

			// Verify signature
			if (!Arrays.equals(BTREE_NODE_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("B tree node signature not matched");
			}

			header.position(4);
			nodeType = header.get();
			nodeLevel = header.get();

			entriesUsed = header.getShort();
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
				throw new HdfException("B tree Raw data not implemented");
			default:
				throw new HdfException("Unreconized node type = " + nodeType);
			}

		} catch (IOException e) {
			// TODO improve message
			throw new HdfException("Error reading B Tree node", e);
		}

	}

	public short getNodeType() {
		return nodeType;
	}

	public short getNodeLevel() {
		return nodeLevel;
	}

	public short getEntriesUsed() {
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

	public long[] getChildAddresses() {
		return childAddresses;
	}

	@Override
	public String toString() {
		return "BTreeNode [address=" + toHex(address) + ", nodeType=" + nodeTypeAsString(nodeType) + ", nodeLevel="
				+ nodeLevel + ", entriesUsed=" + entriesUsed + ", leftSiblingAddress=" + toHex(leftSiblingAddress)
				+ ", rightSiblingAddress=" + toHex(rightSiblingAddress) + "]";
	}

	private String nodeTypeAsString(short nodeType) {
		switch (nodeType) {
		case 0:
			return "GROUP";
		case 1:
			return "DATA";
		default:
			return "UNKNOWN";
		}
	}
}
