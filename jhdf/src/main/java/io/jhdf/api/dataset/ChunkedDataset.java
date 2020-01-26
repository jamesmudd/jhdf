package io.jhdf.api.dataset;

import io.jhdf.api.Dataset;

import java.nio.ByteBuffer;

public interface ChunkedDataset extends Dataset {

    ByteBuffer getRawChunkBuffer(int[] chunkOffset);

}
