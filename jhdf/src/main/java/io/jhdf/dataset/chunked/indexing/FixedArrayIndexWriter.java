/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.BufferBuilder;
import io.jhdf.dataset.chunked.Chunk;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Writes a fixed array chunk index (a 'FAHD' header immediately followed by its 'FADB' data block). Writing counterpart
 * of {@link FixedArrayIndex}.
 *
 * <p>
 * <a href="https://docs.hdfgroup.org/hdf5/develop/_f_m_t3.html#FixedArray">Format Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public final class FixedArrayIndexWriter {

	private static final byte[] FIXED_ARRAY_HEADER_SIGNATURE = "FAHD".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] FIXED_ARRAY_DATA_BLOCK_SIGNATURE = "FADB".getBytes(StandardCharsets.US_ASCII);

	private static final int CLIENT_ID_NON_FILTERED_CHUNKS = 0;
	private static final int CLIENT_ID_FILTERED_CHUNKS = 1;

	// Writing always uses 8 byte offsets and lengths
	private static final int SIZE_OF_OFFSETS = 8;
	private static final int SIZE_OF_LENGTHS = 8;
	private static final int FILTER_MASK_BYTES = 4;

	private FixedArrayIndexWriter() {
		throw new AssertionError("No instances of FixedArrayIndexWriter");
	}

	/**
	 * Serializes a fixed array index for the given chunks. The buffer contains the fixed array header immediately
	 * followed by the data block and should be written at fixedArrayAddress.
	 *
	 * @param chunks the written chunks in chunk index (row major) order
	 * @param fixedArrayAddress the file address the returned buffer will be written to
	 * @param unfilteredChunkSize the size in bytes of a chunk before filters are applied
	 * @param filtered true if a filter pipeline is applied to the chunks
	 * @param pageBits log2 of the number of elements in a data block page. Must be large enough that the index is
	 *                    unpaged i.e. 2^pageBits &gt;= chunks.size()
	 * @return buffer containing the serialized fixed array
	 */
	public static ByteBuffer createFixedArray(List<Chunk> chunks, long fixedArrayAddress, int unfilteredChunkSize, boolean filtered, int pageBits) {
		if (chunks.size() > (1L << pageBits)) {
			// Paged data blocks are not written
			throw new IllegalArgumentException("More chunks [" + chunks.size() + "] than fit in a single page [2^" + pageBits + "]");
		}

		final int entrySize;
		if (filtered) {
			entrySize = SIZE_OF_OFFSETS + chunkSizeLength(unfilteredChunkSize) + FILTER_MASK_BYTES;
		} else {
			entrySize = SIZE_OF_OFFSETS;
		}

		// signature + version + client id + entry size + page bits + max entries + data block address + checksum
		final int headerSize = 4 + 1 + 1 + 1 + 1 + SIZE_OF_LENGTHS + SIZE_OF_OFFSETS + 4;
		final long dataBlockAddress = fixedArrayAddress + headerSize;
		final int clientId = filtered ? CLIENT_ID_FILTERED_CHUNKS : CLIENT_ID_NON_FILTERED_CHUNKS;

		final ByteBuffer headerBuffer = new BufferBuilder()
			.writeBytes(FIXED_ARRAY_HEADER_SIGNATURE)
			.writeByte(0) // Version
			.writeByte(clientId)
			.writeByte(entrySize)
			.writeByte(pageBits)
			.writeUnsignedNumber(chunks.size(), SIZE_OF_LENGTHS) // Max number of entries
			.writeUnsignedNumber(dataBlockAddress, SIZE_OF_OFFSETS)
			.appendChecksum()
			.build();

		final BufferBuilder dataBlockBuilder = new BufferBuilder()
			.writeBytes(FIXED_ARRAY_DATA_BLOCK_SIGNATURE)
			.writeByte(0) // Version
			.writeByte(clientId)
			.writeUnsignedNumber(fixedArrayAddress, SIZE_OF_OFFSETS); // Header address

		for (Chunk chunk : chunks) {
			dataBlockBuilder.writeUnsignedNumber(chunk.getAddress(), SIZE_OF_OFFSETS);
			if (filtered) {
				dataBlockBuilder.writeUnsignedNumber(chunk.getSize(), entrySize - SIZE_OF_OFFSETS - FILTER_MASK_BYTES);
				dataBlockBuilder.writeBitSet(chunk.getFilterMask(), FILTER_MASK_BYTES);
			}
		}

		final ByteBuffer dataBlockBuffer = dataBlockBuilder.appendChecksum().build();

		return new BufferBuilder()
			.writeBuffer(headerBuffer)
			.writeBuffer(dataBlockBuffer)
			.build();
	}

	/**
	 * The number of bytes used to store the size of a filtered chunk in a fixed array element. Matches the
	 * calculation in the HDF5 C library (H5D__farray_idx_create) which allows an extra byte in case the filters
	 * grow the chunk.
	 *
	 * @param unfilteredChunkSize the size in bytes of a chunk before filters are applied
	 * @return the number of bytes used to store filtered chunk sizes
	 */
	public static int chunkSizeLength(int unfilteredChunkSize) {
		// floor(log2(size)) as the position of the highest set bit
		final int log2 = 31 - Integer.numberOfLeadingZeros(unfilteredChunkSize);
		return Math.min(1 + (log2 + 8) / 8, 8);
	}
}
