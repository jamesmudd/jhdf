package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

public class BTreeV1 {
	private static final Logger logger = LoggerFactory.getLogger(BTreeV1.class);

	private static final byte[] BTREE_NODE_V1_SIGNATURE = "TREE".getBytes();

	/** The location of this B tree in the file */
	private final long address;
	/** Type of node. 0 = group, 1 = data */
	private final short nodeType;
	/** Level of the node 0 = leaf */
	private final short nodeLevel;
	private final int entriesUsed;
	private final long leftSiblingAddress;
	private final long rightSiblingAddress;
	private final long[] keys;
	private final long[] childAddresses;

	public static BTreeV1 createBTree(FileChannel fc, Superblock sb, long address) {
		return new BTreeV1(fc, sb, address);
	}

	private BTreeV1(FileChannel fc, Superblock sb, long address) {
		this.address = address;
		try {
			// B Tree Node Header
			int headerSize = 8 + 2 * sb.getSizeOfOffsets();
			ByteBuffer header = ByteBuffer.allocate(headerSize);
			fc.read(header, address);
			header.order(LITTLE_ENDIAN);
			header.rewind();

			// Verify signature
			byte[] formatSignitureByte = new byte[4];
			header.get(formatSignitureByte, 0, formatSignitureByte.length);
			if (!Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignitureByte)) {
				throw new HdfException("B tree V1 node signature not matched");
			}

			nodeType = header.get();
			nodeLevel = header.get();

			entriesUsed = Utils.readBytesAsUnsignedInt(header, 2);
			logger.trace("Entries = {}", getEntriesUsed());

			leftSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
			logger.trace("left address = {}", getLeftSiblingAddress());

			rightSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
			logger.trace("right address = {}", getRightSiblingAddress());

			final int keysAndPointersBytes;
			switch (nodeType) {
			case 0: // Group nodes
				int keyBytes = (2 * entriesUsed + 1) * sb.getSizeOfLengths();
				int childPointerBytes = (2 * entriesUsed) * sb.getSizeOfOffsets();
				keysAndPointersBytes = keyBytes + childPointerBytes;
				break;
			case 1: // Raw data

				throw new UnsupportedHdfException("B tree Raw data not implemented");
			default:
				throw new HdfException("Unreconized node type = " + nodeType);
			}

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
			keys[entriesUsed] = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfLengths());

		} catch (IOException e) {
			throw new HdfException("Error reading B Tree node", e);
		}

	}

	public short getNodeType() {
		return nodeType;
	}

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

	public long[] getChildAddresses() {
		return childAddresses;
	}

	@Override
	public String toString() {
		return "BTreeV1 [address=" + address + ", nodeType=" + nodeType + ", nodeLevel=" + nodeLevel + "]";
	}
}