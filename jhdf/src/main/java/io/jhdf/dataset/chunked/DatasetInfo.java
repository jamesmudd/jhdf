package io.jhdf.dataset.chunked;

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
