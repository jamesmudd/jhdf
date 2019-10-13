#-------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
# 
# http://jhdf.io
# 
# Copyright 2019 James Mudd
# 
# MIT License see 'LICENSE' file
#-------------------------------------------------------------------------------
import h5py

import numpy as np


def write_chunked_datasets(f):

    data = np.arange(15).reshape(5,3)
    large_data = np.arange(1500).reshape(100,5,3)

    # Single chunk index - The current, maximum, and chunk dimension sizes are all the same. Index type 1
    single_chunk_group = f.create_group("single_chunk")
    single_chunk_group.create_dataset("int8", data=data, dtype='i1', chunks=(5,3))
    single_chunk_group.create_dataset("int16", data=data, dtype='i2', chunks=(5,3))
    single_chunk_group.create_dataset("int32", data=data, dtype='i4', chunks=(5,3))
    single_chunk_group.create_dataset('float32', data=data, dtype='f4', chunks=(5,3))
    single_chunk_group.create_dataset('float64', data=data, dtype='f8', chunks=(5,3))

    # Implicit Index - fixed maximum dimension sizes, no filter applied to the dataset,
    # the timing for the space allocation of the dataset chunks is H5P_ALLOC_TIME_EARLY
    # TODO...

    # Fixed Array Index - Fixed maximum dimension sizes. Index type 3
    fixed_array_index_group = f.create_group("fixed_array")
    fixed_array_index_group.create_dataset("int8", data=data, dtype='i1', chunks=(2,3))
    fixed_array_index_group.create_dataset("int16", data=data, dtype='i2', chunks=(2,3))
    fixed_array_index_group.create_dataset("int32", data=data, dtype='i4', chunks=(2,3))
    fixed_array_index_group.create_dataset('float32', data=data, dtype='f4', chunks=(2,3))
    fixed_array_index_group.create_dataset('float64', data=data, dtype='f8', chunks=(2,3))

    filtered_fixed_array_index_group = f.create_group("filtered_fixed_array")
    filtered_fixed_array_index_group.create_dataset("int8", data=data, dtype='i1', chunks=(2,3), compression="gzip")
    filtered_fixed_array_index_group.create_dataset("int16", data=data, dtype='i2', chunks=(2,3), compression="gzip")
    filtered_fixed_array_index_group.create_dataset("int32", data=data, dtype='i4', chunks=(2,3), compression="gzip")
    filtered_fixed_array_index_group.create_dataset('float32', data=data, dtype='f4', chunks=(2,3), compression="gzip")
    filtered_fixed_array_index_group.create_dataset('float64', data=data, dtype='f8', chunks=(2,3), compression="gzip")

    # Extensible Array Index - Only one dimension of unlimited extent. Index type 4
    extensible_array_index_group = f.create_group("extensible_array")
    extensible_array_index_group.create_dataset("int8", data=data, dtype='i1', chunks=(2,3), maxshape=(None,3))
    extensible_array_index_group.create_dataset("int16", data=data, dtype='i2', chunks=(2,3), maxshape=(None,3))
    extensible_array_index_group.create_dataset("int32", data=data, dtype='i4', chunks=(2,3), maxshape=(None,3))
    extensible_array_index_group.create_dataset('float32', data=data, dtype='f4', chunks=(2,3), maxshape=(None,3))
    extensible_array_index_group.create_dataset('float64', data=data, dtype='f8', chunks=(2,3), maxshape=(None,3))
    # large data to use secondary blocks smallest chunk size
    extensible_array_index_group.create_dataset("large_int16", data=large_data, dtype='i2', chunks=(1,1,1), maxshape=(None,5,3))

    # B Tree V2 Index - More than one dimension of unlimited extent. Index type 5
    # btree_v2_index_group = f.create_group("btree_v2")
    # btree_v2_index_group.create_dataset("int8", data=data, dtype='i1', chunks=(2,3), maxshape=(None,None))
    # btree_v2_index_group.create_dataset("int16", data=data, dtype='i2', chunks=(2,3), maxshape=(None,None))
    # btree_v2_index_group.create_dataset("int32", data=data, dtype='i4', chunks=(2,3), maxshape=(None,None))
    # btree_v2_index_group.create_dataset('float32', data=data, dtype='f4', chunks=(2,3), maxshape=(None,None))
    # btree_v2_index_group.create_dataset('float64', data=data, dtype='f8', chunks=(2,3), maxshape=(None,None))

    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making chunked v4 dataset test files...')

    f = h5py.File('chunked_v4_datasets.hdf5', 'w', libver='latest')
    write_chunked_datasets(f)
    print('created chunked_v4_datasets.hdf5')

