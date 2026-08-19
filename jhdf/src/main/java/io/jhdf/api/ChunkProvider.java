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
 * Supplies a chunked dataset's data one chunk at a time.
 * <p>
 * Passing a dataset to {@link WritableGroup#putDataset(String, Object)} requires the whole thing to be in memory,
 * which puts an upper bound on what can be written. A {@code ChunkProvider} is asked for one chunk at a time while
 * the file is written, so the memory needed is that of a single chunk rather than the whole dataset. This suits
 * data that is read from elsewhere or generated on demand.
 * <p>
 * Every chunk of the dataset is requested exactly once, so the provider must be able to supply any chunk; returning
 * {@code null} is an error and fails the write.
 *
 * @since v0.14.0
 */
@FunctionalInterface
public interface ChunkProvider {

	/**
	 * Provides the data for a single chunk.
	 * <p>
	 * The returned array must have the chunk dimensions given in the {@link DatasetCreationOptions} and hold the
	 * dataset's type, so for a {@code double} dataset with chunk dimensions {@code {16, 32}} it is a
	 * {@code double[16][32]}. Where a chunk overhangs the edge of the dataset it is still a whole chunk; the
	 * values outside the dataset are stored but never read back.
	 *
	 * @param chunkOffset the offset of this chunk within the dataset, one element per dimension
	 * @return the chunk's data, never {@code null}
	 */
	Object getChunk(long[] chunkOffset);
}
