/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class DataLayoutMessage extends Message {

	public DataLayoutMessage(BitSet flags) {
		super(flags);
	}

	public abstract DataLayout getDataLayout();

	public static DataLayoutMessage createDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		final byte version = bb.get();

		if (version != 3 && version != 4) {
			throw new UnsupportedHdfException(
					"Only v3 and v4 data layout messages are supported. Detected version = " + version);
		}

		final byte layoutClass = bb.get();

		switch (layoutClass) {
		case 0: // Compact Storage
			return new CompactDataLayoutMessage(bb, flags);
		case 1: // Contiguous Storage
			return new ContiguousDataLayoutMessage(bb, sb, flags);
		case 2: // Chunked Storage
			if (version == 3) {
				return new ChunkedDataLayoutMessageV3(bb, sb, flags);
			} else { // v4
				return new ChunkedDataLayoutMessageV4(bb, sb, flags);
			}
		case 3: // Virtual storage
			throw new UnsupportedHdfException("Virtual storage is not supported");
		default:
			throw new UnsupportedHdfException("Unknown storage layout " + layoutClass);
		}
	}

	public static class CompactDataLayoutMessage extends DataLayoutMessage {

		private final ByteBuffer dataBuffer;

		private CompactDataLayoutMessage(ByteBuffer bb, BitSet flags) {
			super(flags);
			final int compactDataSize = Utils.readBytesAsUnsignedInt(bb, 2);
			dataBuffer = Utils.createSubBuffer(bb, compactDataSize);
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.COMPACT;
		}

		public ByteBuffer getDataBuffer() {
			return dataBuffer;
		}
	}

	public static class ContiguousDataLayoutMessage extends DataLayoutMessage {

		private final long address;
		private final long size;

		private ContiguousDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);
			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			size = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CONTIGUOUS;
		}

		public long getAddress() {
			return address;
		}

		public long getSize() {
			return size;
		}
	}

	public static class ChunkedDataLayoutMessageV3 extends DataLayoutMessage {

		private final long address;
		private final int size;
		private final int[] chunkDimensions;

		private ChunkedDataLayoutMessageV3(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);
			final int chunkDimensionality = bb.get() - 1;
			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			chunkDimensions = new int[chunkDimensionality];
			for (int i = 0; i < chunkDimensions.length; i++) {
				chunkDimensions[i] = Utils.readBytesAsUnsignedInt(bb, 4);
			}
			size = Utils.readBytesAsUnsignedInt(bb, 4);
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CHUNKED;
		}

		public long getBTreeAddress() {
			return address;
		}

		public int getSize() {
			return size;
		}

		public int[] getChunkDimensions() {
			return chunkDimensions;
		}
	}

	public static class ChunkedDataLayoutMessageV4 extends DataLayoutMessage {

		private static final int DONT_FILTER_PARTIAL_BOUND_CHUNKS = 0;
		private static final int SINGLE_INDEX_WITH_FILTER = 1;

		private final long address;
		private final byte indexingType;
		private final int[] chunkDimensions;

		private byte pageBits;
		private byte maxBits;
		private byte indexElements;
		private byte minPointers;
		private byte minElements;
		private int nodeSize;
		private byte splitPercent;
		private byte mergePercent;

		// Fields only for filtered single chunk
		private boolean isFilteredSingleChunk = false;
		private int sizeOfFilteredSingleChunk;
		private BitSet filterMaskFilteredSingleChunk;

		private ChunkedDataLayoutMessageV4(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);

			final BitSet chunkedFlags = BitSet.valueOf(new byte[] { bb.get() });
			final int chunkDimensionality = bb.get();
			final int dimSizeBytes = bb.get();

			chunkDimensions = new int[chunkDimensionality];
			for (int i = 0; i < chunkDimensions.length; i++) {
				chunkDimensions[i] = Utils.readBytesAsUnsignedInt(bb, dimSizeBytes);
			}

			indexingType = bb.get();

			switch (indexingType) {
			case 1: // Single Chunk
				if (chunkedFlags.get(SINGLE_INDEX_WITH_FILTER)) {
					isFilteredSingleChunk = true;
					sizeOfFilteredSingleChunk = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfLengths());
					filterMaskFilteredSingleChunk = BitSet.valueOf(new byte[] { bb.get(), bb.get(), bb.get(), bb.get() });
				}
				break;

			case 2: // Implicit
				break; // There is nothing for this case

			case 3: // Fixed Array
				pageBits = bb.get();
				break;

			case 4: // Extensible Array
				maxBits = bb.get();
				indexElements = bb.get();
				minPointers = bb.get();
				minElements = bb.get();
				pageBits = bb.get(); // This is wrong in the spec says 2 bytes its actually 1
				break;

			case 5: // B tree v2
				nodeSize = bb.getInt();
				splitPercent = bb.get();
				mergePercent = bb.get();
				break;

			default:
				throw new UnsupportedHdfException("Unrecognized chunk indexing type. type=" + indexingType);
			}

			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CHUNKED;
		}

		public long getAddress() {
			return address;
		}

		public byte getPageBits() {
			return pageBits;
		}

		public byte getMaxBits() {
			return maxBits;
		}

		public byte getIndexElements() {
			return indexElements;
		}

		public byte getMinPointers() {
			return minPointers;
		}

		public byte getMinElements() {
			return minElements;
		}

		public int getNodeSize() {
			return nodeSize;
		}

		public byte getSplitPercent() {
			return splitPercent;
		}

		public byte getMergePercent() {
			return mergePercent;
		}

		public byte getIndexingType() {
			return indexingType;
		}

		public int[] getChunkDimensions() {
			return chunkDimensions;
		}

		public int getSizeOfFilteredSingleChunk() {
			if(!isFilteredSingleChunk) {
				throw new HdfException("Requested size of filtered single chunk when its not set.");
			}
			return sizeOfFilteredSingleChunk;
		}

		public BitSet getFilterMaskFilteredSingleChunk() {
			if(!isFilteredSingleChunk){
				throw new HdfException("Requested filter mask of filtered single chunk when its not set.");
			}
			return filterMaskFilteredSingleChunk;
		}

		public boolean isFilteredSingleChunk() {
			return isFilteredSingleChunk;
		}
	}

}
