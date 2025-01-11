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
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImplicitChunkIndex implements ChunkIndex {

	private final int chunkSize;
	private final int[] datasetDimensions;
	private final int[] chunkDimensions;
	private final long baseAddress;

	public ImplicitChunkIndex(long baseAddress, DatasetInfo datasetInfo) {
		this.baseAddress = baseAddress;
		this.chunkSize = datasetInfo.getChunkSizeInBytes();
		this.datasetDimensions = datasetInfo.getDatasetDimensions();
		this.chunkDimensions = datasetInfo.getChunkDimensions();
	}

	@Override
	public Collection<Chunk> getAllChunks() {
		int totalChunks = Utils.totalChunks(datasetDimensions, chunkDimensions);
		List<Chunk> chunks = new ArrayList<>(totalChunks);
		for (int i = 0; i < totalChunks; i++) {
			chunks.add(new ChunkImpl(baseAddress + i* chunkSize,
				chunkSize,
				Utils.chunkIndexToChunkOffset(i, chunkDimensions, datasetDimensions)));
		}
		return chunks;
	}

}
