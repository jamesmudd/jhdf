/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api.dataset;

import io.jhdf.api.Dataset;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;

/**
 * Interface for chunked datasets
 *
 * @author James Mudd
 */
public interface ChunkedDataset extends Dataset {

	/**
	 * Gets the chunk dimensions of this dataset.
	 *
	 * @return the chunk dimensions of this dataset
	 */
	int[] getChunkDimensions();

	/**
	 * Gets the raw buffer for the specified chunk.
	 *
	 * @param chunkOffset the offset of the required chunk
	 * @return the raw buffer for this chunk
	 * @throws HdfException If the chunk offset is not valid for this dataset
	 */
	ByteBuffer getRawChunkBuffer(int[] chunkOffset);

	/**
	 * Gets the decompressed byte array for the specified chunk.
	 *
	 * @param chunkOffset the offset of the required chunk
	 * @return the decompressed byte array for this chunk
	 * @throws HdfException If the chunk offset is not valid for this dataset
	 */
	byte[] getDecompressedChunk(int[] chunkOffset);

}
