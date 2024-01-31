/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.api;

import io.jhdf.storage.HdfFileChannel;

public interface WritableNode extends Node {

	long write(HdfFileChannel hdfFileChannel, long position);
}
