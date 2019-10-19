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
import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.indexing.ChunkImpl;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * V1 B-trees where the node type is 1 i.e. points to raw data chunk nodes
 *
 * @author James Mudd
 */
public abstract class BTreeV1Data extends BTreeV1 {

	private BTreeV1Data(HdfFileChannel hdfFc, long address) {
		super(hdfFc, address);
	}

	/**
	 * @return the raw data chunks address from this b-tree
	 */
	public abstract List<Chunk> getChunks();

	/* package */ static class BTreeV1DataLeafNode extends BTreeV1Data {

		private final ArrayList<Chunk> chunks;

		/* package */ BTreeV1DataLeafNode(HdfFileChannel hdfFc, long address, int dataDimensions) {
			super(hdfFc, address);

			final int keySize = 4 + 4 + (dataDimensions + 1) * 8;
			final int keyBytes = (entriesUsed + 1) * keySize;
			final int childPointerBytes = entriesUsed * hdfFc.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			final long keysAddress = address + 8 + 2 * hdfFc.getSizeOfOffsets();
			final ByteBuffer bb = hdfFc.readBufferFromAddress(keysAddress, keysAndPointersBytes);

			chunks = new ArrayList<>(entriesUsed);
			for (int i = 0; i < entriesUsed; i++) {
				Chunk chunk = readKeyAsChunk(hdfFc.getSuperblock(), dataDimensions, bb);
				chunks.add(chunk);
			}

			bb.position(bb.position() + keySize);
		}

		private Chunk readKeyAsChunk(Superblock sb, int dataDimensions, ByteBuffer bb) {
			final int chunkSize = Utils.readBytesAsUnsignedInt(bb, 4);
			final BitSet filterMask = BitSet.valueOf(new byte[] { bb.get(), bb.get(), bb.get(), bb.get() });
			final int[] chunkOffset = new int[dataDimensions];
			for (int j = 0; j < dataDimensions; j++) {
				chunkOffset[j] = Utils.readBytesAsUnsignedInt(bb, 8);
			}
			long zero = Utils.readBytesAsUnsignedLong(bb, 8);
			if (zero != 0) {
				throw new HdfException("Invalid B tree chunk detected");
			}

			final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			return new ChunkImpl(chunkAddress, chunkSize, chunkOffset, filterMask);
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

		/* package */ BTreeV1DataNonLeafNode(HdfFileChannel hdfFc, long address, int dataDimensions) {
			super(hdfFc, address);

			final int keySize = 4 + 4 + (dataDimensions + 1) * 8;
			final int keyBytes = (entriesUsed + 1) * keySize;
			final int childPointerBytes = entriesUsed * hdfFc.getSizeOfOffsets();
			final int keysAndPointersBytes = keyBytes + childPointerBytes;

			final long keysAddress = address + 8 + 2 * hdfFc.getSizeOfOffsets();
			final ByteBuffer keysAndPointersBuffer = hdfFc.readBufferFromAddress(keysAddress, keysAndPointersBytes);

			childNodes = new ArrayList<>(entriesUsed);

			for (int i = 0; i < entriesUsed; i++) {
				keysAndPointersBuffer.position(keysAndPointersBuffer.position() + keySize);
				long childAddress = Utils.readBytesAsUnsignedLong(keysAndPointersBuffer, hdfFc.getSizeOfOffsets());
				childNodes.add(BTreeV1.createDataBTree(hdfFc, childAddress, dataDimensions));
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
