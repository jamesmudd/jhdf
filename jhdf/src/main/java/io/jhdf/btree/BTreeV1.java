package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

public abstract class BTreeV1 {
	private static final Logger logger = LoggerFactory.getLogger(BTreeV1.class);

	private static final byte[] BTREE_NODE_V1_SIGNATURE = "TREE".getBytes();

	/** The location of this B tree in the file */
	private final long address;
	/** Type of node. 0 = group, 1 = data */
	private short nodeType;
	/** Level of the node 0 = leaf */
	private short nodeLevel;
	protected int entriesUsed;
	private long leftSiblingAddress;
	private long rightSiblingAddress;

	public static BTreeV1 createGroupBTree(FileChannel fc, Superblock sb, long address) {
		// B Tree Node Header
		int headerSize = 6;
		ByteBuffer header = ByteBuffer.allocate(headerSize);
		try {
			fc.read(header, address);
		} catch (IOException e) {
			throw new HdfException("Error reading BTreeV1 header at address: " + address);
		}
		header.order(LITTLE_ENDIAN);
		header.rewind();

		// Verify signature
		byte[] formatSignitureByte = new byte[4];
		header.get(formatSignitureByte, 0, formatSignitureByte.length);
		if (!Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignitureByte)) {
			throw new HdfException("B tree V1 node signature not matched");
		}

		final byte nodeType = header.get();
		final byte nodeLevel = header.get();

		switch (nodeType) {
		case 0: // Group nodes
			if (nodeLevel > 0) {
				return new BTreeV1GroupNonLeafNode(fc, sb, address);
			} else {
				return new BTreeV1GroupLeafNode(fc, sb, address);
			}
		case 1: // Raw data chunk nodes
			throw new UnsupportedHdfException("Raw data not supported yet");
		default:
			throw new HdfException("Unreconized BTreeV1 node type: " + nodeType);
		}
	}

	public static BTreeV1Data createDataBTree(FileChannel fc, Superblock sb, long address, int dataDimensions) {
		// B Tree Node Header
		int headerSize = 6;
		ByteBuffer header = ByteBuffer.allocate(headerSize);
		try {
			fc.read(header, address);
		} catch (IOException e) {
			throw new HdfException("Error reading BTreeV1 header at address: " + address);
		}
		header.order(LITTLE_ENDIAN);
		header.rewind();

		// Verify signature
		byte[] formatSignitureByte = new byte[4];
		header.get(formatSignitureByte, 0, formatSignitureByte.length);
		if (!Arrays.equals(BTREE_NODE_V1_SIGNATURE, formatSignitureByte)) {
			throw new HdfException("B tree V1 node signature not matched");
		}

		final byte nodeType = header.get();
		final byte nodeLevel = header.get();

		switch (nodeType) {
		case 0: // Group nodes
			throw new HdfException("Not a data B Tree");
		case 1: // Raw data chunk nodes
			if (nodeLevel > 0) {
				return new BTreeV1Data.BTreeV1DataNonLeafNode(fc, sb, address, dataDimensions);
			} else {
				return new BTreeV1Data.BTreeV1DataLeafNode(fc, sb, address, dataDimensions);
			}
		default:
			throw new HdfException("Unreconized BTreeV1 node type: " + nodeType);
		}
	}

	/* package */ BTreeV1(FileChannel fc, Superblock sb, long address) {
		this.address = address;

		int headerSize = 8 * sb.getSizeOfOffsets();
		ByteBuffer header = ByteBuffer.allocate(headerSize);
		try {
			fc.read(header, address + 6);
		} catch (IOException e) {
			throw new HdfException("Error reading BTreeV1 header at address: " + address);
		}
		header.order(LITTLE_ENDIAN);
		header.rewind();

		entriesUsed = Utils.readBytesAsUnsignedInt(header, 2);
		logger.trace("Entries = {}", entriesUsed);

		leftSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
		logger.trace("left address = {}", leftSiblingAddress);

		rightSiblingAddress = Utils.readBytesAsUnsignedLong(header, sb.getSizeOfOffsets());
		logger.trace("right address = {}", rightSiblingAddress);

	}

	public abstract List<Long> getChildAddresses();

	@Override
	public String toString() {
		return "BTreeV1 [address=" + address + ", nodeType=" + nodeType + ", nodeLevel=" + nodeLevel + "]";
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

	public long getAddress() {
		return address;
	}

	private static class BTreeV1GroupLeafNode extends BTreeV1 {

		private final List<Long> childAddresses;

		private BTreeV1GroupLeafNode(FileChannel fc, Superblock sb, long address) {
			super(fc, sb, address);

			int keyBytes = (2 * entriesUsed + 1) * sb.getSizeOfLengths();
			int childPointerBytes = (2 * entriesUsed) * sb.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			ByteBuffer keysAndPointersBuffer = ByteBuffer.allocate(keysAndPointersBytes);
			try {
				fc.read(keysAndPointersBuffer, address + 8 + 2 * sb.getSizeOfOffsets());
			} catch (IOException e) {
				throw new HdfException("Error reading BTreeV1NonLeafNode at address: " + address);
			}
			keysAndPointersBuffer.order(LITTLE_ENDIAN);
			keysAndPointersBuffer.rewind();

			childAddresses = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + sb.getSizeOfLengths());
				childAddresses.add(Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfOffsets()));
			}
		}

		@Override
		public List<Long> getChildAddresses() {
			return childAddresses;
		}

	}

	private static class BTreeV1GroupNonLeafNode extends BTreeV1 {

		private final List<BTreeV1> childNodes;

		private BTreeV1GroupNonLeafNode(FileChannel fc, Superblock sb, long address) {
			super(fc, sb, address);

			int keyBytes = (2 * entriesUsed + 1) * sb.getSizeOfLengths();
			int childPointerBytes = (2 * entriesUsed) * sb.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			ByteBuffer keysAndPointersBuffer = ByteBuffer.allocate(keysAndPointersBytes);
			try {
				fc.read(keysAndPointersBuffer, address + 8 + 2 * sb.getSizeOfOffsets());
			} catch (IOException e) {
				throw new HdfException("Error reading BTreeV1NonLeafNode at address: " + address);
			}
			keysAndPointersBuffer.order(LITTLE_ENDIAN);
			keysAndPointersBuffer.rewind();

			childNodes = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + sb.getSizeOfOffsets());
				long childAddress = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfOffsets());
				childNodes.add(createGroupBTree(fc, sb, childAddress));
			}

		}

		@Override
		public List<Long> getChildAddresses() {
			List<Long> childAddresses = new ArrayList<>();
			for (BTreeV1 child : childNodes) {
				childAddresses.addAll(child.getChildAddresses());
			}
			return childAddresses;
		}
	}

}