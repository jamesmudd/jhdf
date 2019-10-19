/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import java.util.BitSet;

public interface Chunk {
    int getSize();

    BitSet getFilterMask();

    int[] getChunkOffset();

    long getAddress();
}
