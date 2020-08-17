/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import java.util.BitSet;

public interface Chunk {

    /**
     * Gets the size of this chink in bytes
     *
     * @return the size of the chunk in bytes
     */
    int getSize();

    BitSet getFilterMask();

    /**
     * Gets this chunks offset within the dataset
     *
     * @return this chunks offset
     */
    int[] getChunkOffset();

    /**
     * Gets the address of this chunk in the file
     *
     * @return the chunks address
     */
    long getAddress();
}
