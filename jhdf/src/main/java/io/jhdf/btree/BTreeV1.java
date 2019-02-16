package io.jhdf.btree;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
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

	public static BTreeV1 createBTree(FileChannel fc, Superblock sb, long address) {
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
				return new BTreeV1DataNonLeafNode(fc, sb, address, dataDimensions);
			} else {
				return new BTreeV1DataLeafNode(fc, sb, address, dataDimensions);
			}
		default:
			throw new HdfException("Unreconized BTreeV1 node type: " + nodeType);
		}
	}

	private BTreeV1(FileChannel fc, Superblock sb, long address) {
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
				childNodes.add(createBTree(fc, sb, childAddress));
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

	public abstract static class BTreeV1Data extends BTreeV1 {

		public BTreeV1Data(FileChannel fc, Superblock sb, long address, int dataDimensions) {
			super(fc, sb, address);
		}

		public abstract List<Chunk> getChunks();

		public class Chunk {
			private final int size;
			private final BitSet filterMask;
			private final long[] chunkOffset;
			private final long address;

			private Chunk(int size, BitSet filterMask, long[] chunkOffset, long address) {
				super();
				this.size = size;
				this.filterMask = filterMask;
				this.chunkOffset = chunkOffset;
				this.address = address;
			}

			public int getSize() {
				return size;
			}

			public BitSet getFilterMask() {
				return filterMask;
			}

			public long[] getChunkOffset() {
				return chunkOffset;
			}

			public long getAddress() {
				return address;
			}

			@Override
			public String toString() {
				return "Chunk [chunkOffset=" + Arrays.toString(chunkOffset) + ", size=" + size + ", address=" + address
						+ "]";
			}
		}
	}

	private static class BTreeV1DataLeafNode extends BTreeV1Data {

		private final ArrayList<Chunk> chunks;

		private BTreeV1DataLeafNode(FileChannel fc, Superblock sb, long address, int dataDimensions) {
			super(fc, sb, address, dataDimensions);

			int keySize = 4 + 4 + (dataDimensions + 1) * 8;
			int keyBytes = (entriesUsed + 1) * keySize;
			int childPointerBytes = entriesUsed * sb.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			ByteBuffer bb = ByteBuffer.allocate(keysAndPointersBytes);
			try {
				fc.read(bb, address + 8 + 2 * sb.getSizeOfOffsets());
			} catch (IOException e) {
				throw new HdfException("Error reading BTreeV1LeafNode at address: " + address);
			}
			bb.order(LITTLE_ENDIAN);
			bb.rewind();

			chunks = new ArrayList<>(entriesUsed);
			for (int i = 0; i < entriesUsed; i++) {
				Chunk chunk = readKeyAsChunk(sb, dataDimensions, bb);
				chunks.add(chunk);
			}

			bb.position(bb.position() + keySize);
		}

		private Chunk readKeyAsChunk(Superblock sb, int dataDimensions, ByteBuffer bb) {
			int chunkSize = Utils.readBytesAsUnsignedInt(bb, 4);
			BitSet filterMask = BitSet.valueOf(new byte[] { bb.get(), bb.get(), bb.get(), bb.get() });
			long[] chunkOffset = new long[dataDimensions];
			for (int j = 0; j < dataDimensions; j++) {
				chunkOffset[j] = Utils.readBytesAsUnsignedLong(bb, 8);
			}
			long zero = Utils.readBytesAsUnsignedLong(bb, 8);
			if (zero != 0) {
				throw new HdfException("Invalid B tree chunk detected");
			}

			long chunkAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			return new Chunk(chunkSize, filterMask, chunkOffset, chunkAddress);
		}

		@Override
		public List<Long> getChildAddresses() {
			return chunks.stream().map(Chunk::getAddress).collect(toList());
		}

		@Override
		public List<Chunk> getChunks() {
			return chunks;
		}
	}

	private static class BTreeV1DataNonLeafNode extends BTreeV1Data {

		private final List<BTreeV1Data> childNodes;

		public BTreeV1DataNonLeafNode(FileChannel fc, Superblock sb, long address, int dataDimensions) {
			super(fc, sb, address, dataDimensions);

			int keySize = 4 + 4 + (dataDimensions + 1) * 8;
			int keyBytes = (entriesUsed + 1) * keySize;
			int childPointerBytes = entriesUsed * sb.getSizeOfOffsets();
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
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + keySize);
				long childAddress = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, sb.getSizeOfOffsets());
				childNodes.add(createDataBTree(fc, sb, childAddress, dataDimensions));
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

		@Override
		public List<Chunk> getChunks() {
			List<Chunk> childAddresses = new ArrayList<>();
			for (BTreeV1Data child : childNodes) {
				childAddresses.addAll(child.getChunks());
			}
			return childAddresses;
		}

	}

}