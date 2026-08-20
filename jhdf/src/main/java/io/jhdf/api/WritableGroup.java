/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
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
	 Put a named dataset into the group specifying how it is stored e.g. chunked and compressed, and/or forcing the
	 underlying fixed point (integer) data type to be written as unsigned. See {@link DatasetCreationOptions}.

	 * @param name The dataset name within this group
	 * @param data The dataset array
	 * @param options Options controlling how the dataset is stored e.g. chunking, filters and unsigned fixed point types
	 * @return the dataset, for further modification
	 * @since v0.13.0
	 */
	WritableDataset putDataset(String name, Object data, DatasetCreationOptions options);

	/**
	 Put a named chunked dataset into the group whose data is supplied one chunk at a time, so a dataset larger
	 than memory can be written. The dataset's shape and type are given here rather than inferred, because no data
	 object exists yet; the {@link ChunkProvider} is asked for every chunk while the file is written.

	 * @param name The dataset name within this group
	 * @param javaType The dataset's element type e.g. {@code double.class}
	 * @param dimensions The dataset's dimensions
	 * @param options Options controlling how the dataset is stored, must specify chunk dimensions
	 * @param chunkProvider Supplies each chunk when the file is written
	 * @return the dataset, for further modification
	 * @since v0.14.0
	 */
	WritableDataset putDataset(String name, Class<?> javaType, int[] dimensions, DatasetCreationOptions options,
							   ChunkProvider chunkProvider);

	/**
	 Create a chunked dataset in this group written a chunk at a time, as the data becomes available, rather than
	 from a dataset already in memory. Chunks are written to the file as they are handed over, so the memory needed
	 is that of a single chunk.
	 <p>
	 The returned dataset must be closed, with every chunk written, before the file is closed.

	 * @param name The dataset name within this group
	 * @param javaType The dataset's element type e.g. {@code double.class}
	 * @param dimensions The dataset's dimensions
	 * @param options Options controlling how the dataset is stored, must specify chunk dimensions
	 * @return the dataset to write chunks to
	 * @since v0.14.0
	 */
	StreamingDataset newStreamingDataset(String name, Class<?> javaType, int[] dimensions,
										 DatasetCreationOptions options);

	WritableGroup putGroup(String name);

}
