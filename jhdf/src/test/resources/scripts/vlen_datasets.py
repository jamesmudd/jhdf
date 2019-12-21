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


# The idea of this test if to write variable length (ragged array) datasets
def write_vlen_datasets(f):

    uint8_vlen_type = h5py.vlen_dtype(np.uint8)
    uint8_vlen_dataset_chunked = f.create_dataset("vlen_uint8_data", (3,), dtype=uint8_vlen_type)
    uint8_vlen_dataset_chunked[0] = [0]
    uint8_vlen_dataset_chunked[1] = [1, 2]
    uint8_vlen_dataset_chunked[2] = [3, 4, 5]

    uint16_vlen_type_chunked = h5py.vlen_dtype(np.uint16)
    uint16_vlen_dataset = f.create_dataset("vlen_uint16_data", (3,), dtype=uint16_vlen_type_chunked)
    uint16_vlen_dataset[0] = [0]
    uint16_vlen_dataset[1] = [1, 2]
    uint16_vlen_dataset[2] = [3, 4, 5]

    uint32_vlen_type = h5py.vlen_dtype(np.uint32)
    uint32_vlen_dataset_chunked = f.create_dataset("vlen_uint32_data", (3,), dtype=uint32_vlen_type)
    uint32_vlen_dataset_chunked[0] = [0]
    uint32_vlen_dataset_chunked[1] = [1, 2]
    uint32_vlen_dataset_chunked[2] = [3, 4, 5]

    uint64_vlen_type = h5py.vlen_dtype(np.uint64)
    uint64_vlen_dataset_chunked = f.create_dataset("vlen_uint64_data", (3,), dtype=uint64_vlen_type)
    uint64_vlen_dataset_chunked[0] = [0]
    uint64_vlen_dataset_chunked[1] = [1, 2]
    uint64_vlen_dataset_chunked[2] = [3, 4, 5]

    float32_vlen_type = h5py.vlen_dtype(np.float32)
    float32_vlen_dataset_chunked = f.create_dataset("vlen_float32_data", (3,), dtype=float32_vlen_type)
    float32_vlen_dataset_chunked[0] = [0]
    float32_vlen_dataset_chunked[1] = [1, 2]
    float32_vlen_dataset_chunked[2] = [3, 4, 5]

    float64_vlen_type = h5py.vlen_dtype(np.float64)
    float64_vlen_dataset_chunked = f.create_dataset("vlen_float64_data", (3,), dtype=float64_vlen_type)
    float64_vlen_dataset_chunked[0] = [0]
    float64_vlen_dataset_chunked[1] = [1, 2]
    float64_vlen_dataset_chunked[2] = [3, 4, 5]

    # Chunked
    uint8_vlen_type = h5py.vlen_dtype(np.uint8)
    uint8_vlen_dataset_chunked = f.create_dataset("vlen_uint8_data_chunked", (3,), dtype=uint8_vlen_type, chunks=(3,))
    uint8_vlen_dataset_chunked[0] = [0]
    uint8_vlen_dataset_chunked[1] = [1, 2]
    uint8_vlen_dataset_chunked[2] = [3, 4, 5]

    uint16_vlen_type_chunked = h5py.vlen_dtype(np.uint16)
    uint16_vlen_dataset = f.create_dataset("vlen_uint16_data_chunked", (3,), dtype=uint16_vlen_type_chunked, chunks=(3,))
    uint16_vlen_dataset[0] = [0]
    uint16_vlen_dataset[1] = [1, 2]
    uint16_vlen_dataset[2] = [3, 4, 5]

    uint32_vlen_type = h5py.vlen_dtype(np.uint32)
    uint32_vlen_dataset_chunked = f.create_dataset("vlen_uint32_data_chunked", (3,), dtype=uint32_vlen_type, chunks=(3,))
    uint32_vlen_dataset_chunked[0] = [0]
    uint32_vlen_dataset_chunked[1] = [1, 2]
    uint32_vlen_dataset_chunked[2] = [3, 4, 5]

    uint64_vlen_type = h5py.vlen_dtype(np.uint64)
    uint64_vlen_dataset_chunked = f.create_dataset("vlen_uint64_data_chunked", (3,), dtype=uint64_vlen_type, chunks=(3,))
    uint64_vlen_dataset_chunked[0] = [0]
    uint64_vlen_dataset_chunked[1] = [1, 2]
    uint64_vlen_dataset_chunked[2] = [3, 4, 5]

    float32_vlen_type = h5py.vlen_dtype(np.float32)
    float32_vlen_dataset_chunked = f.create_dataset("vlen_float32_data_chunked", (3,), dtype=float32_vlen_type, chunks=(3,))
    float32_vlen_dataset_chunked[0] = [0]
    float32_vlen_dataset_chunked[1] = [1, 2]
    float32_vlen_dataset_chunked[2] = [3, 4, 5]

    float64_vlen_type = h5py.vlen_dtype(np.float64)
    float64_vlen_dataset_chunked = f.create_dataset("vlen_float64_data_chunked", (3,), dtype=float64_vlen_type, chunks=(3,))
    float64_vlen_dataset_chunked[0] = [0]
    float64_vlen_dataset_chunked[1] = [1, 2]
    float64_vlen_dataset_chunked[2] = [3, 4, 5]

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making variable lentgh dataset test files...')

    f = h5py.File('test_vlen_datasets_earliest.hdf5', 'w', libver='earliest')
    write_vlen_datasets(f)
    print('created test_vlen_datasets_earliest.hdf5')

    f = h5py.File('test_vlen_datasets_latest.hdf5', 'w', libver='latest')
    write_vlen_datasets(f)
    print('created test_vlen_datasets_latest.hdf5')
