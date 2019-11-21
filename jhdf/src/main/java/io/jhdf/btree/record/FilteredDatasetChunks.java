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
import java.util.BitSet;

/**
 * Version 2 B-tree, Type 11 Record Layout - Filtered Dataset Chunks
 *
 * @author James Mudd
 */
public class FilteredDatasetChunks extends BTreeDatasetChunkRecord {

    private final Chunk chunk;

    public FilteredDatasetChunks(ByteBuffer buffer, DatasetInfo datasetInfo) {
        final long address = Utils.readBytesAsUnsignedLong(buffer, 8); // size of offsets

        final int chunkSizeBytes = buffer.limit()
                - 8 // size of offsets
                - 4 // filter mask
                - datasetInfo.getDatasetDimensions().length * 8; // dimension offsets

        final int chunkSize = Utils.readBytesAsUnsignedInt(buffer, chunkSizeBytes);
        final BitSet filterMask = BitSet.valueOf(new byte[] { buffer.get(), buffer.get(), buffer.get(), buffer.get() });

        int[] chunkOffset = new int[datasetInfo.getDatasetDimensions().length];
        for (int i = 0; i < chunkOffset.length; i++) {
            chunkOffset[i] = Utils.readBytesAsUnsignedInt(buffer, 8) * datasetInfo.getChunkDimensions()[i];
        }

        chunk = new ChunkImpl(address, chunkSize, chunkOffset, filterMask);

    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }
}
