package io.jhdf.btree.record;

import io.jhdf.dataset.chunked.Chunk;

public abstract class BTreeDatasetChunkRecord extends  BTreeRecord {

    public abstract Chunk getChunk();
}
