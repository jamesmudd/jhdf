package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;

import java.util.Collection;

public interface ChunkIndex {

    Collection<Chunk> getAllChunks();

}
