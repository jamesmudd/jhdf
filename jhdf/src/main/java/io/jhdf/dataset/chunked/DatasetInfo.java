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


/**
 * Data class for passing dataset information
 *
 * @author James Mudd
 */
public class DatasetInfo {

	private final int chunkSizeInBytes;
	private final long[] datasetDimensions;
	private final int[] chunkDimensions;

	public DatasetInfo(int chunkSizeInBytes, long[] datasetDimensions, int[] chunkDimensions) {
		this.chunkSizeInBytes = chunkSizeInBytes;
		this.datasetDimensions = datasetDimensions == null ? null : datasetDimensions.clone();
		this.chunkDimensions = chunkDimensions == null ? null : chunkDimensions.clone();
	}

	public int getChunkSizeInBytes() {
		return chunkSizeInBytes;
	}

	public long[] getDatasetDimensions() {
		return datasetDimensions == null ? null : datasetDimensions.clone();
	}

	public int[] getChunkDimensions() {
		return chunkDimensions == null ? null : chunkDimensions.clone();
	}
}
