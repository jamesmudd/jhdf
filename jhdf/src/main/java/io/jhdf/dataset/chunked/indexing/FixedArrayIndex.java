/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
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
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(FixedArrayIndex.class);

	private final long address;
	private final int unfilteredChunkSize;

	private final int[] datasetDimensions;
	private final int[] chunkDimensions;

	private final int clientId;
	private final int entrySize;
	private final int pageBits;
	private final int maxNumberOfEntries;
	private final long dataBlockAddress;
	private final int pages;
	private final int pageSize;

	private final FixedArrayDataBlockInitializer dataBlockInitializer;

	public FixedArrayIndex(HdfBackingStorage hdfBackingStorage, long address, DatasetInfo datasetInfo) {
		this.address = address;
		this.unfilteredChunkSize = datasetInfo.getChunkSizeInBytes();
		this.datasetDimensions = datasetInfo.getDatasetDimensions();
		this.chunkDimensions = datasetInfo.getChunkDimensions();

		final int headerSize = 12 + hdfBackingStorage.getSizeOfOffsets() + hdfBackingStorage.getSizeOfLengths();
		final ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, headerSize);
		bb.mark();

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
		pageSize = 1 << pageBits;
		pages = (maxNumberOfEntries + pageSize -1) / pageSize;

		dataBlockAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());

		// Checksum
		ChecksumUtils.validateChecksumFromMark(bb);

		dataBlockInitializer = new FixedArrayDataBlockInitializer(hdfBackingStorage);
		logger.info("Read fixed array index header. pages=[{}], maxEntries=[{}]", pages, maxNumberOfEntries);
	}

	private class FixedArrayDataBlockInitializer extends LazyInitializer<FixedArrayDataBlock> {

		private final HdfBackingStorage hdfBackingStorage;

		public FixedArrayDataBlockInitializer(HdfBackingStorage hdfBackingStorage) {
			this.hdfBackingStorage = hdfBackingStorage;
		}

		@Override
		protected FixedArrayDataBlock initialize() {
			logger.info("Initializing data block");
			return new FixedArrayDataBlock(hdfBackingStorage, FixedArrayIndex.this.dataBlockAddress);
		}

	}

	private class FixedArrayDataBlock {

		private final List<Chunk> chunks = new ArrayList<>(maxNumberOfEntries);

		private FixedArrayDataBlock(HdfBackingStorage hdfBackingStorage, long address) {

			int pageBitmapBytes = (pages + 7) / 8;
			int headerSize = 6 + hdfBackingStorage.getSizeOfOffsets() + FixedArrayIndex.this.entrySize * FixedArrayIndex.this.maxNumberOfEntries + 4;
			if(pages > 1) {
				headerSize += pageBitmapBytes + (4 * pages);
			}
			final ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, headerSize);
			bb.mark();

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

			final int dataBlockclientId = bb.get();
			if (dataBlockclientId != FixedArrayIndex.this.clientId) {
				throw new HdfException("Fixed array client ID mismatch. Possible file corruption detected");
			}

			final long headerAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());
			if (headerAddress != FixedArrayIndex.this.address) {
				throw new HdfException("Fixed array data block header address missmatch");
			}

			if(pages > 1) {
				readPaged(hdfBackingStorage, pageBitmapBytes, bb, dataBlockclientId);
			} else {
				// Unpaged
				logger.info("Reading unpaged");
				if (dataBlockclientId == 0) { // Not filtered
					for (int i = 0; i < FixedArrayIndex.this.maxNumberOfEntries; i++) {
						readUnfiltered(hdfBackingStorage.getSizeOfOffsets(), bb, i);
					}
				} else if (dataBlockclientId == 1) { // Filtered
					for (int i = 0; i < FixedArrayIndex.this.maxNumberOfEntries; i++) {
						readFiltered(hdfBackingStorage, bb, i);
					}
				} else {
					throw new HdfException("Unrecognized client ID  = " + dataBlockclientId);
				}

				ChecksumUtils.validateChecksumFromMark(bb);
			}
		}

		private void readPaged(HdfBackingStorage hdfBackingStorage, int pageBitmapBytes, ByteBuffer bb, int dataBlockclientId) {
			logger.info("Reading paged");
			byte[] pageBitmap = new byte[pageBitmapBytes];
			bb.get(pageBitmap);

			ChecksumUtils.validateChecksumFromMark(bb);

			int chunkIndex = 0;
			for(int page = 0; page < pages; page++) {
				final int currentPageSize;
				if(page == pages -1) {
					// last page so not a full page
					currentPageSize = FixedArrayIndex.this.maxNumberOfEntries % FixedArrayIndex.this.pageSize;
				} else {
					currentPageSize = FixedArrayIndex.this.pageSize;
				}

				if (dataBlockclientId == 0) { // Not filtered
					for (int i = 0; i < currentPageSize; i++) {
						readUnfiltered(hdfBackingStorage.getSizeOfOffsets(), bb, chunkIndex++);
					}
				} else if (dataBlockclientId == 1) { // Filtered
					for (int i = 0; i < currentPageSize; i++) {
						readFiltered(hdfBackingStorage, bb, chunkIndex++);
					}
				} else {
					throw new HdfException("Unrecognized client ID  = " + dataBlockclientId);
				}
				ChecksumUtils.validateChecksumFromMark(bb);
			}
		}

		private void readFiltered(HdfBackingStorage hdfBackingStorage, ByteBuffer bb, int i) {
			final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, hdfBackingStorage.getSizeOfOffsets());
			final int chunkSizeInBytes = Utils.readBytesAsUnsignedInt(bb, FixedArrayIndex.this.entrySize - hdfBackingStorage.getSizeOfOffsets() - 4);
			final BitSet filterMask = BitSet.valueOf(new byte[]{bb.get(), bb.get(), bb.get(), bb.get()});
			final int[] chunkOffset = Utils.chunkIndexToChunkOffset(i, FixedArrayIndex.this.chunkDimensions, FixedArrayIndex.this.datasetDimensions);

			chunks.add(new ChunkImpl(chunkAddress, chunkSizeInBytes, chunkOffset, filterMask));
		}

		private void readUnfiltered(int sizeOfOffsets, ByteBuffer bb, int chunkIndex) {
			final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, sizeOfOffsets);
			final int[] chunkOffset = Utils.chunkIndexToChunkOffset(chunkIndex, FixedArrayIndex.this.chunkDimensions, FixedArrayIndex.this.datasetDimensions);
			chunks.add(new ChunkImpl(chunkAddress, FixedArrayIndex.this.unfilteredChunkSize, chunkOffset));
		}
	}

	@Override
	public Collection<Chunk> getAllChunks() {
		try {
			return this.dataBlockInitializer.get().chunks;
		} catch (ConcurrentException e) {
			throw new HdfException("Error initializing data block", e);
		}
	}
}
