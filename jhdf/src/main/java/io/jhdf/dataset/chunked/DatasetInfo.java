/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
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
    private final int[] datasetDimensions;
    private final int[] chunkDimensions;

    public DatasetInfo(int chunkSizeInBytes, int[] datasetDimensions, int[] chunkDimensions) {
        this.chunkSizeInBytes = chunkSizeInBytes;
        this.datasetDimensions = datasetDimensions;
        this.chunkDimensions = chunkDimensions;
    }

    public int getChunkSizeInBytes() {
        return chunkSizeInBytes;
    }

    public int[] getDatasetDimensions() {
        return datasetDimensions;
    }

    public int[] getChunkDimensions() {
        return chunkDimensions;
    }
}
