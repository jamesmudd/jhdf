/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api;

/**
 * A chunked dataset written a chunk at a time, as the data becomes available.
 * <p>
 * {@link ChunkProvider} covers the case where any chunk can be produced on request. This covers the other one: data
 * that arrives over time, from an instrument or a running computation, where the application drives and jHDF
 * follows. Each chunk is written to the file as it is handed over, so the memory needed is that of a single chunk.
 * <p>
 * The dataset must be closed before the file is, and every chunk must have been written by then. A chunk that is
 * never written fails the close rather than being left as fill, because a dataset that silently reads back as zeros
 * is worse than an error.
 *
 * @since v0.14.0
 */
public interface StreamingDataset extends WritableDataset, AutoCloseable {

	/**
	 * Writes one chunk of the dataset.
	 * <p>
	 * The data must have the chunk dimensions given when the dataset was created and hold the dataset's type. Chunks
	 * may be written in any order, but each exactly once.
	 *
	 * @param chunkOffset the offset of this chunk within the dataset, one element per dimension
	 * @param data the chunk's data
	 */
	void writeChunk(long[] chunkOffset, Object data);

	/**
	 * Finishes the dataset, writing its chunk index. The dataset cannot be written to afterwards.
	 */
	@Override
	void close();
}
