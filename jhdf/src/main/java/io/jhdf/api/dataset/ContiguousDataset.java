/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
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


	/**
	 * Gets the address of the data in the HDF5-file relative to the end of the userblock. To get the absolute data
	 * address in the file, {@code file.getUserBlockSize()} needs to be added.
	 *
	 * @return the address where the data of this contiguous dataset starts relative to the userblock
	 */
	long getDataAddress();
}
