/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree.record;

import io.jhdf.Utils;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;
import io.jhdf.dataset.chunked.indexing.ChunkImpl;

import java.nio.ByteBuffer;

/**
 * Version 2 B-tree, Type 10 Record Layout - Non-filtered Dataset Chunks
 *
 * @author James Mudd
 */
public class NonFilteredDatasetChunks extends BTreeDatasetChunkRecord {

    private final Chunk chunk;

    public NonFilteredDatasetChunks(ByteBuffer buffer, DatasetInfo datasetInfo) {
        final long address = Utils.readBytesAsUnsignedLong(buffer, 8); // size of offsets

        int[] chunkOffset = new int[datasetInfo.getDatasetDimensions().length];
        for (int i = 0; i < chunkOffset.length; i++) {
            chunkOffset[i] = Utils.readBytesAsUnsignedInt(buffer, 8) * datasetInfo.getChunkDimensions()[i];
        }

        chunk = new ChunkImpl(address, datasetInfo.getChunkSizeInBytes(), chunkOffset);
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }
}
