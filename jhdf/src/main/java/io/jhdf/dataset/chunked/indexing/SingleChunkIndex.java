/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;

import java.util.Collection;
import java.util.Collections;

public class SingleChunkIndex implements ChunkIndex {

    private final Chunk singleChunk;

    public SingleChunkIndex(ChunkedDataLayoutMessageV4 layoutMessageV4, int unfilteredChunkSize, int[] dimensions) {
        final int[] chunkOffset = new int[dimensions.length]; // Single chunk so zero offset
        if (layoutMessageV4.isFilteredSingleChunk()) {
            this.singleChunk = new ChunkImpl(layoutMessageV4.getAddress(), layoutMessageV4.getSizeOfFilteredSingleChunk(), chunkOffset, layoutMessageV4.getFilterMaskFilteredSingleChunk());
        } else {
            this.singleChunk = new ChunkImpl(layoutMessageV4.getAddress(), unfilteredChunkSize, chunkOffset);
        }
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return Collections.singletonList(singleChunk);
    }

}
