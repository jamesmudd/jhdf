package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;

import java.util.Collection;
import java.util.Collections;

public class EmptyChunkIndex implements ChunkIndex {

    @Override
    public Collection<Chunk> getAllChunks() {
        return Collections.emptyList();
    }
}
