/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import java.util.BitSet;

public interface Chunk {

	/**
	 * Gets the size of this chink in bytes
	 *
	 * @return the size of the chunk in bytes
	 */
	int getSize();

	/**
	 * Gets the per-chunk filter mask. A set bit means that the filter at the
	 * corresponding position in the original filter pipeline was not applied to
	 * this chunk and must be skipped during decoding.
	 *
	 * @return the per-chunk filter mask
	 */
	BitSet getFilterMask();

	/**
	 * Gets this chunks offset within the dataset
	 *
	 * @return this chunks offset
	 */
	long[] getChunkOffset();

	/**
	 * Gets the address of this chunk in the file
	 *
	 * @return the chunks address
	 */
	long getAddress();
}
