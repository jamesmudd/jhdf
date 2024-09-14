# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# https://jhdf.io
#
# Copyright (c) 2024 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py

import numpy as np


def write_chunked_datasets(f):
    # Less than 1025 element should be unpaged
    data = np.arange(1000).reshape(10, 100)
    # >1024 elements paged
    two_page_data = np.arange(2000).reshape(200, 10)
    five_page_data = np.arange(5000).reshape(200, 25)

    # Fixed Array Index - Fixed maximum dimension sizes. Index type 3
    fixed_array_group = f.create_group("fixed_array")
    fixed_array_group.create_dataset("int8_unpaged", data=data, dtype='i1', chunks=(2, 3))
    fixed_array_group.create_dataset("int8_two_page", data=two_page_data, dtype='i1', chunks=(1, 1))
    fixed_array_group.create_dataset("int8_five_page", data=five_page_data, dtype='i1', chunks=(1, 1))

    filtered_fixed_array_group = f.create_group("filtered_fixed_array")
    filtered_fixed_array_group.create_dataset("int8_unpaged", data=data, dtype='i1', chunks=(2, 3), compression="gzip")
    filtered_fixed_array_group.create_dataset("int8_two_page", data=two_page_data, dtype='i1', chunks=(1, 1), compression="gzip")
    filtered_fixed_array_group.create_dataset("int8_five_page", data=five_page_data, dtype='i1', chunks=(1, 1), compression="gzip")

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making chunked v4 dataset test files...')

    f = h5py.File('fixed_array_paged_datasets.hdf5', 'w', libver='latest')
    write_chunked_datasets(f)
    print('fixed_array_paged_datasets.hdf5')
