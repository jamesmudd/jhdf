/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api.dataset;

import io.jhdf.api.Dataset;

import java.nio.ByteBuffer;

/**
 * Interface for contiguous datasets
 *
 * @author James Mudd
 */
public interface ContiguousDataset extends Dataset {

    /**
     * Gets the buffer for this dataset
     *
     * @return the buffer for this dataset
     */
    ByteBuffer getBuffer();

}
