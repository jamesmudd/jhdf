package io.jhdf.btree;

import java.util.BitSet;

public interface Chunk {
    int getSize();

    BitSet getFilterMask();

    int[] getChunkOffset();

    long getAddress();
}
