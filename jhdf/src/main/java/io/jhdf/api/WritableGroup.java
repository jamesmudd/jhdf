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
	 latter case, you can specialize what type of dataset you want to provide.

	 * @param name The dataset name within this group
	 * @param data The dataset array or implementation
	 * @return the dataset, for further modification
	 */
	WritableDataset putDataset(String name, Object data);

	/**
	 Put a named dataset into the group specifying how it is stored e.g. chunked and compressed. See
	 {@link DatasetCreationOptions}.

	 * @param name The dataset name within this group
	 * @param data The dataset array
	 * @param options Options controlling how the dataset is stored e.g. chunking and filters
	 * @return the dataset, for further modification
	 * @since v0.13.0
	 */
	WritableDataset putDataset(String name, Object data, DatasetCreationOptions options);

	/**
	 Put a named dataset into the group, optionally forcing the underlying fixed point
	 (integer) data type to be written as unsigned. This only applies when {@code data} is a
	 supported fixed point (integer) array/scalar (byte/short/int/long); requesting
	 {@code unsigned} for any other data type will result in an exception.

	 * @param name The dataset name within this group
	 * @param data The dataset array or scalar, must be a fixed point (integer) type when {@code unsigned} is {@code true}
	 * @param unsigned if {@code true} the dataset will be written as an unsigned fixed point type
	 * @return the dataset, for further modification
	 */
	WritableDataset putDataset(String name, Object data, boolean unsigned);

	WritableGroup putGroup(String name);

}
