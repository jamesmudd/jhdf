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

public interface WritableGroup extends Group, WritableNode {

	WritiableDataset putDataset(String name, Object data);

	WritiableDataset putDataset(WritiableDataset dataset);


	WritableGroup putGroup(String name);
}
