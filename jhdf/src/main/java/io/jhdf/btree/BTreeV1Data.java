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

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

/**
 * V1 B-trees where the node type is 1 i.e. points to raw data chunk nodes
 * 
 * @author James Mudd
 */
public abstract class BTreeV1Data extends BTreeV1 {

	private BTreeV1Data(FileChannel fc, Superblock sb, long address) {
		super(fc, sb, address);
	}

	/**
	 * @return the raw data chunks address from this b-tree
	 */
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

	/* package */ static class BTreeV1DataLeafNode extends BTreeV1Data {

		private final ArrayList<Chunk> chunks;

		/* package */ BTreeV1DataLeafNode(FileChannel fc, Superblock sb, long address, int dataDimensions) {
			super(fc, sb, address);

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

	/* package */ static class BTreeV1DataNonLeafNode extends BTreeV1Data {

		private final List<BTreeV1Data> childNodes;

		/* package */ /**
						 * @param fc
						 * @param sb
						 * @param address
						 * @param dataDimensions
						 */
		BTreeV1DataNonLeafNode(FileChannel fc, Superblock sb, long address, int dataDimensions) {
			super(fc, sb, address);

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
				childNodes.add(BTreeV1.createDataBTree(fc, sb, childAddress, dataDimensions));
			}
		}

		@Override
		public List<Chunk> getChunks() {
			List<Chunk> childAddresses = new ArrayList<>();
			for (BTreeV1Data child : childNodes) {
				childAddresses.addAll(child.getChunks());
			}
			return childAddresses;
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