package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

public class SingleChunkIndex implements ChunkIndex {

    private final Chunk singleChunk;

    public SingleChunkIndex(long chunkAddress, int chunkSize, int[] dimensions) {
        final int[] chunkOffset = new int[dimensions.length]; // Single chunk so zero offset
        this.singleChunk = new ChunkImpl(chunkAddress, chunkSize, chunkOffset);
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return Collections.singletonList(singleChunk);
    }

}
