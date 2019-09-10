package io.jhdf.dataset.chunked;

import java.util.BitSet;

public interface Chunk {
    int getSize();

    BitSet getFilterMask();

    int[] getChunkOffset();

    long getAddress();
}
