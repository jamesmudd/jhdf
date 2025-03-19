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

	WritableDataset putDataset(String name, Object data);

	WritableGroup putGroup(String name);

	WritableDataset putWritableDataset(String name, WritableDataset dsb);
}
