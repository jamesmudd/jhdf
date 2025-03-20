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

	/**
	 Put a named dataset into the group. The data object can either be any valid hdf5 dataset type,
	 or it can be an instance of {@link WritableDataset}. In the former case, the dataset details
	 are inferred from the provided data and a default implementation is provided for you. In the
	 latter case, you can specialize what type of dataset you want to provide. This can be used,
	 for example to enable efficient bulk writes for handling data larger than heap, with
	 {@link io.jhdf.api.dataset.StreamableDataset};

	 * @param name The dataset name within this group
	 * @param data The dataset array or implementation
	 * @return the dataset, for further modification
	 */
	WritableDataset putDataset(String name, Object data);

	WritableGroup putGroup(String name);

}
