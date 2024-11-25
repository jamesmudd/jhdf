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

import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;

import java.util.Collection;
import java.util.Collections;

public class ImplicitChunkIndex implements ChunkIndex {

	private final int unfilteredChunkSize;
	private final int[] datasetDimensions;
	private final int[] chunkDimensions;
	private final long baseAddress;

	public ImplicitChunkIndex(long baseAddress, DatasetInfo datasetInfo) {

		this.baseAddress = baseAddress;
		this.unfilteredChunkSize = datasetInfo.getChunkSizeInBytes();
		this.datasetDimensions = datasetInfo.getDatasetDimensions();
		this.chunkDimensions = datasetInfo.getChunkDimensions();
	}

	@Override
	public Collection<Chunk> getAllChunks() {
		return Collections.emptyList();
	}

}
