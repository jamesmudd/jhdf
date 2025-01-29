/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.api;

public interface WritiableDataset extends Dataset, WritableNode {

    public void setChunked(boolean chunked);

    public void setCompression(int compression);

}
