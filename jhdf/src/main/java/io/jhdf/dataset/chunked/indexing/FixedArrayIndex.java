/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.Utils;
import io.jhdf.checksum.ChecksumUtils;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class FixedArrayIndex implements ChunkIndex {

	private static final byte[] FIXED_ARRAY_HEADER_SIGNATURE = "FAHD".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] FIXED_ARRAY_DATA_BLOCK_SIGNATURE = "FADB".getBytes(StandardCharsets.US_ASCII);

	private final long address;
	private final int unfilteredChunkSize;

	private final int[] datasetDimensions;
	private final int[] chunkDimensions;

	private final int clientId;
	private final int entrySize;
	private final int pageBits;
	private final int maxNumberOfEntries;
	private final long dataBlockAddress;

	private final List<Chunk> chunks;

	public FixedArrayIndex(HdfBackingStorage hdfBackingStorage, long address, DatasetInfo datasetInfo) {
		this.address = address;
		this.unfilteredChunkSize = datasetInfo.getChunkSizeInBytes();
		this.datasetDimensions = datasetInfo.getDatasetDimensions();
		this.chunkDimensions = datasetInfo.getChunkDimensions();

		final int headerSize = 12 + hdfBackingStorage.getSizeOfOffsets() + hdfBackingStorage.getSizeOfLengths();
		final ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, headerSize);

		byte[] formatSignatureBytes = new byte[4];
		bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);

		// Verify signature
		if (!Arrays.equals(FIXED_ARRAY_HEADER_SIGNATURE, formatSignatureBytes)) {
			throw new HdfException("Fixed array header signature 'FAHD' not matched, at address " + address);
		}

		// Version Number
		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unsupported fixed array index version detected. Version: " + version);
		}

		clientId = bb.get();
		entrySize = bb.get();
		pageBits = bb.get();

		maxNumberOfEntries = Utils.readBytesAsUnsignedInt(bb, hdfBackingStorage.getSizeOfLengths());
		dataBlockAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());

		chunks = new ArrayList<>(maxNumberOfEntries);

		// Checksum
		bb.rewind();
		ChecksumUtils.validateChecksum(bb);

		// Building the object fills the chunks. Probably shoudld be changed
		new FixedArrayDataBlock(this, hdfBackingStorage, dataBlockAddress);
	}

	private static class FixedArrayDataBlock {

		private FixedArrayDataBlock(FixedArrayIndex fixedArrayIndex, HdfBackingStorage hdfBackingStorage, long address) {

			// TODO header size ignoring paging
			final int headerSize = 6 + hdfBackingStorage.getSizeOfOffsets() + fixedArrayIndex.entrySize * fixedArrayIndex.maxNumberOfEntries + 4;
			final ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, headerSize);

			byte[] formatSignatureBytes = new byte[4];
			bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);

			// Verify signature
			if (!Arrays.equals(FIXED_ARRAY_DATA_BLOCK_SIGNATURE, formatSignatureBytes)) {
				throw new HdfException("Fixed array data block signature 'FADB' not matched, at address " + address);
			}

			// Version Number
			final byte version = bb.get();
			if (version != 0) {
				throw new HdfException("Unsupported fixed array data block version detected. Version: " + version);
			}

			final int clientId = bb.get();
			if (clientId != fixedArrayIndex.clientId) {
				throw new HdfException("Fixed array client ID mismatch. Possible file corruption detected");
			}

			final long headerAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());
			if (headerAddress != fixedArrayIndex.address) {
				throw new HdfException("Fixed array data block header address missmatch");
			}

			// TODO ignoring paging here might need to revisit

			if (clientId == 0) { // Not filtered
				for (int i = 0; i < fixedArrayIndex.maxNumberOfEntries; i++) {
					final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());
					final int[] chunkOffset = Utils.chunkIndexToChunkOffset(i, fixedArrayIndex.chunkDimensions, fixedArrayIndex.datasetDimensions);
					fixedArrayIndex.chunks.add(new ChunkImpl(chunkAddress, fixedArrayIndex.unfilteredChunkSize, chunkOffset));
				}
			} else if (clientId == 1) { // Filtered
				for (int i = 0; i < fixedArrayIndex.maxNumberOfEntries; i++) {
					final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());
					final int chunkSizeInBytes = Utils.readBytesAsUnsignedInt(bb, fixedArrayIndex.entrySize - hdfBackingStorage.getSizeOfOffsets() - 4);
					final BitSet filterMask = BitSet.valueOf(new byte[]{bb.get(), bb.get(), bb.get(), bb.get()});
					final int[] chunkOffset = Utils.chunkIndexToChunkOffset(i, fixedArrayIndex.chunkDimensions, fixedArrayIndex.datasetDimensions);

					fixedArrayIndex.chunks.add(new ChunkImpl(chunkAddress, chunkSizeInBytes, chunkOffset, filterMask));
				}
			} else {
				throw new HdfException("Unrecognized client ID  = " + clientId);
			}

			bb.rewind();
			ChecksumUtils.validateChecksum(bb);
		}
	}

	@Override
	public Collection<Chunk> getAllChunks() {
		return chunks;
	}
}
