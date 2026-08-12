/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.BufferBuilder;
import io.jhdf.Constants;
import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class DataLayoutMessage extends Message {

	public static final int MESSAGE_TYPE = 8;

	public DataLayoutMessage(BitSet flags) {
		super(flags);
	}

	public abstract DataLayout getDataLayout();

	public static DataLayoutMessage createDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		final byte version = bb.get();

		switch (version) {
			case 1:
			case 2:
				return readV1V2Message(bb, sb, flags);
			case 3:
			case 4:
				return readV3V4Message(bb, sb, flags, version);
			default:
				throw new UnsupportedHdfException("Unsupported data layout message version detected. Detected version = " + version);
		}
	}

	private static DataLayoutMessage readV1V2Message(ByteBuffer bb, Superblock sb, BitSet flags) {
		byte dimensionality = bb.get(); // for chunked is +1 than actual dims

		final byte layoutClass = bb.get();

		bb.position(bb.position() + 5); // skip reserved bytes

		final long dataAddress;
		if (layoutClass != 0) { // not compact
			dataAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		} else {
			dataAddress = Constants.UNDEFINED_ADDRESS;
		}

		// If chunked value stored is +1 so correct it here
		if (layoutClass == 2) {
			dimensionality--;
		}

		int[] dimensions = new int[dimensionality];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = Utils.readBytesAsUnsignedInt(bb, 4);
		}

		switch (layoutClass) {
			case 0: // Compact Storage
				final int compactDataSize = Utils.readBytesAsUnsignedInt(bb, 4);
				final ByteBuffer compactDataBuffer = Utils.createSubBuffer(bb, compactDataSize);
				return new CompactDataLayoutMessage(flags, compactDataBuffer);
			case 1: // Contiguous
				return new ContiguousDataLayoutMessage(flags, dataAddress, -1L);
			case 2: // Chunked
				final int dataElementSize = Utils.readBytesAsUnsignedInt(bb, 4);
				return new ChunkedDataLayoutMessage(flags, dataAddress, dataElementSize, dimensions);
			default:
				throw new UnsupportedHdfException("Unknown storage layout " + layoutClass);
		}
	}

	private static DataLayoutMessage readV3V4Message(ByteBuffer bb, Superblock sb, BitSet flags, byte version) {
		final byte layoutClass = bb.get();

		switch (layoutClass) {
			case 0: // Compact Storage
				return new CompactDataLayoutMessage(bb, flags);
			case 1: // Contiguous Storage
				return new ContiguousDataLayoutMessage(bb, sb, flags);
			case 2: // Chunked Storage
				if (version == 3) {
					return new ChunkedDataLayoutMessage(bb, sb, flags);
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

		private CompactDataLayoutMessage(BitSet flags, ByteBuffer dataBuffer) {
			super(flags);
			this.dataBuffer = dataBuffer;
		}

		private CompactDataLayoutMessage(ByteBuffer bb, BitSet flags) {
			super(flags);
			final int compactDataSize = Utils.readBytesAsUnsignedInt(bb, 2);
			this.dataBuffer = Utils.createSubBuffer(bb, compactDataSize);
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.COMPACT;
		}

		public ByteBuffer getDataBuffer() {
			return dataBuffer.slice();
		}

		@Override
		public ByteBuffer toBuffer() {
			return null;
		}
	}

	public static class ContiguousDataLayoutMessage extends DataLayoutMessage {

		private final long address;
		private final long size;

		private ContiguousDataLayoutMessage(BitSet flags, long address, long size) {
			super(flags);
			this.address = address;
			this.size = size;
		}

		private ContiguousDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);
			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			size = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
		}

		public static ContiguousDataLayoutMessage create(long address, long size) {
			return new ContiguousDataLayoutMessage(Message.BASIC_FLAGS, address, size);
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CONTIGUOUS;
		}

		public long getAddress() {
			return address;
		}

		/**
		 * @return size in bytes if known or -1 otherwise
		 */
		public long getSize() {
			return size;
		}

		@Override
		public ByteBuffer toBuffer() {
			return new BufferBuilder()
				.writeByte(3) // Version
				.writeByte(1) // Contiguous Storage
				.writeLong(address)
				.writeLong(size)
				.build();
		}
	}

	public static class ChunkedDataLayoutMessage extends DataLayoutMessage {

		private final long bTreeAddress;
		private final int size;
		private final int[] chunkDimensions;

		public ChunkedDataLayoutMessage(BitSet flags, long bTreeAddress, int size, int[] chunkDimensions) {
			super(flags);
			this.bTreeAddress = bTreeAddress;
			this.size = size;
			this.chunkDimensions = chunkDimensions == null ? null : chunkDimensions.clone();
		}

		private ChunkedDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);
			final int chunkDimensionality = bb.get() - 1;
			bTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
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
			return bTreeAddress;
		}

		public int getSize() {
			return size;
		}

		public int[] getChunkDimensions() {
			return chunkDimensions == null ? null : chunkDimensions.clone();
		}

		@Override
		public ByteBuffer toBuffer() {
			return null;
		}
	}

	public static class ChunkedDataLayoutMessageV4 extends DataLayoutMessage {

		private static final int DONT_FILTER_PARTIAL_BOUND_CHUNKS = 0;
		private static final int SINGLE_INDEX_WITH_FILTER = 1;

		public static final byte SINGLE_CHUNK_INDEX = 1;
		public static final byte IMPLICIT_INDEX = 2;
		public static final byte FIXED_ARRAY_INDEX = 3;
		public static final byte EXTENSIBLE_ARRAY_INDEX = 4;
		public static final byte B_TREE_V2_INDEX = 5;

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

		/**
		 * For writing. chunkDimensions must include the trailing element size 'dimension'.
		 */
		private ChunkedDataLayoutMessageV4(long address, byte indexingType, int[] chunkDimensions) {
			super(Message.BASIC_FLAGS);
			this.address = address;
			this.indexingType = indexingType;
			this.chunkDimensions = ArrayUtils.clone(chunkDimensions);
		}

		/**
		 * Creates a single chunk (index type 1) layout message for an unfiltered dataset.
		 *
		 * @param address the address of the chunk data
		 * @param chunkDimensions the chunk dimensions with the element size appended
		 * @return the message
		 */
		public static ChunkedDataLayoutMessageV4 createSingleChunk(long address, int[] chunkDimensions) {
			return new ChunkedDataLayoutMessageV4(address, SINGLE_CHUNK_INDEX, chunkDimensions);
		}

		/**
		 * Creates a single chunk (index type 1) layout message for a filtered dataset.
		 *
		 * @param address the address of the chunk data
		 * @param chunkDimensions the chunk dimensions with the element size appended
		 * @param sizeOfFilteredChunk the size in bytes of the filtered (e.g. compressed) chunk
		 * @return the message
		 */
		public static ChunkedDataLayoutMessageV4 createFilteredSingleChunk(long address, int[] chunkDimensions, int sizeOfFilteredChunk) {
			ChunkedDataLayoutMessageV4 message = new ChunkedDataLayoutMessageV4(address, SINGLE_CHUNK_INDEX, chunkDimensions);
			message.isFilteredSingleChunk = true;
			message.sizeOfFilteredSingleChunk = sizeOfFilteredChunk;
			message.filterMaskFilteredSingleChunk = new BitSet(32); // All filters applied
			return message;
		}

		/**
		 * Creates a fixed array (index type 3) layout message.
		 *
		 * @param address the address of the fixed array header
		 * @param chunkDimensions the chunk dimensions with the element size appended
		 * @param pageBits the number of bits needed to store the maximum number of elements in a data block page
		 * @return the message
		 */
		public static ChunkedDataLayoutMessageV4 createFixedArray(long address, int[] chunkDimensions, int pageBits) {
			ChunkedDataLayoutMessageV4 message = new ChunkedDataLayoutMessageV4(address, FIXED_ARRAY_INDEX, chunkDimensions);
			message.pageBits = (byte) pageBits;
			return message;
		}

		private ChunkedDataLayoutMessageV4(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);

			final BitSet chunkedFlags = BitSet.valueOf(new byte[]{bb.get()});
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
						filterMaskFilteredSingleChunk = BitSet.valueOf(new byte[]{bb.get(), bb.get(), bb.get(), bb.get()});
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
			return chunkDimensions == null ? null : chunkDimensions.clone();
		}

		public int getSizeOfFilteredSingleChunk() {
			if (!isFilteredSingleChunk) {
				throw new HdfException("Requested size of filtered single chunk when its not set.");
			}
			return sizeOfFilteredSingleChunk;
		}

		public BitSet getFilterMaskFilteredSingleChunk() {
			if (!isFilteredSingleChunk) {
				throw new HdfException("Requested filter mask of filtered single chunk when its not set.");
			}
			return filterMaskFilteredSingleChunk;
		}

		public boolean isFilteredSingleChunk() {
			return isFilteredSingleChunk;
		}

		@Override
		public ByteBuffer toBuffer() {
			final BitSet chunkedFlags = new BitSet(8);
			chunkedFlags.set(SINGLE_INDEX_WITH_FILTER, isFilteredSingleChunk);

			final int dimSizeBytes = getDimensionSizeEncodedLength();

			final BufferBuilder bufferBuilder = new BufferBuilder()
				.writeByte(4) // Version
				.writeByte(2) // Chunked storage
				.writeBitSet(chunkedFlags, 1)
				.writeByte(chunkDimensions.length)
				.writeByte(dimSizeBytes);

			for (int chunkDimension : chunkDimensions) {
				bufferBuilder.writeUnsignedNumber(chunkDimension, dimSizeBytes);
			}

			bufferBuilder.writeByte(indexingType);

			switch (indexingType) {
				case SINGLE_CHUNK_INDEX:
					if (isFilteredSingleChunk) {
						bufferBuilder.writeLong(sizeOfFilteredSingleChunk); // Size of lengths
						bufferBuilder.writeBitSet(filterMaskFilteredSingleChunk, 4);
					}
					break;

				case FIXED_ARRAY_INDEX:
					bufferBuilder.writeByte(pageBits);
					break;

				default:
					throw new UnsupportedHdfException("Writing chunk indexing type not supported. type=" + indexingType);
			}

			return bufferBuilder
				.writeLong(address) // Size of offsets
				.build();
		}

		/**
		 * The number of bytes used to encode each chunk dimension. Matches the calculation
		 * in the HDF5 C library (H5D__chunk_set_sizes).
		 */
		private int getDimensionSizeEncodedLength() {
			int maxEncodedBytes = 1;
			for (int chunkDimension : chunkDimensions) {
				// floor(log2(dim)) as the position of the highest set bit
				final int log2 = 31 - Integer.numberOfLeadingZeros(chunkDimension);
				final int encodedBytes = (log2 + 8) / 8;
				maxEncodedBytes = Math.max(maxEncodedBytes, encodedBytes);
			}
			return maxEncodedBytes;
		}
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}


}
