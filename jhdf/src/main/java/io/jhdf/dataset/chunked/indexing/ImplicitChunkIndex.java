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

import io.jhdf.Utils;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ImplicitChunkIndex implements ChunkIndex {

	private final int chunkSize;
	private final long[] datasetDimensions;
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
		final long totalChunks = Utils.totalChunks(datasetDimensions, chunkDimensions);
		return new AbstractCollection<Chunk>() {
			@Override
			public Iterator<Chunk> iterator() {
				return new Iterator<Chunk>() {
					private long index = 0;

					@Override
					public boolean hasNext() {
						return index < totalChunks;
					}

					@Override
					public Chunk next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						long chunkIndex = index++;
						long chunkAddress = Math.addExact(baseAddress,
							Math.multiplyExact(chunkIndex, (long) chunkSize));
						return new ChunkImpl(chunkAddress,
							chunkSize,
							Utils.chunkIndexToChunkOffset(chunkIndex, chunkDimensions, datasetDimensions));
					}
				};
			}

			@Override
			public int size() {
				return totalChunks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalChunks;
			}

			@Override
			public boolean isEmpty() {
				return totalChunks == 0;
			}
		};
	}

}
