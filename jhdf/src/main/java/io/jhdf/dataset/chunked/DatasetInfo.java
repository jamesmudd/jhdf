/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Data class for passing dataset information
 *
 * @author James Mudd
 */
public class DatasetInfo {

	private final int chunkSizeInBytes;
	private final int[] datasetDimensions;
	private final int[] chunkDimensions;

	public DatasetInfo(int chunkSizeInBytes, int[] datasetDimensions, int[] chunkDimensions) {
		this.chunkSizeInBytes = chunkSizeInBytes;
		this.datasetDimensions = ArrayUtils.clone(datasetDimensions);
		this.chunkDimensions = ArrayUtils.clone(chunkDimensions);
	}

	public int getChunkSizeInBytes() {
		return chunkSizeInBytes;
	}

	public int[] getDatasetDimensions() {
		return ArrayUtils.clone(datasetDimensions);
	}

	public int[] getChunkDimensions() {
		return ArrayUtils.clone(chunkDimensions);
	}
}
