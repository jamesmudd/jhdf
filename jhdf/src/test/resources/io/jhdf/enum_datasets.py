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

'''
The idea of this test if to write enum datasets
'''
def write_enum_datasets(f):

    data = np.arange(4)

    uint8_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint8)
    f.create_dataset("enum_uint8_data", data=data, dtype=uint8_enum_type)
    uint16_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint16)
    f.create_dataset("enum_uint16_data", data=data, dtype=uint16_enum_type)
    uint32_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint32)
    f.create_dataset("enum_uint32_data", data=data, dtype=uint32_enum_type)
    uint64_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint64)
    f.create_dataset("enum_uint64_data", data=data, dtype=uint64_enum_type)

    data = np.arange(4).reshape(2,2)

    uint8_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint8)
    f.create_dataset("2d_enum_uint8_data", data=data, dtype=uint8_enum_type)
    uint16_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint16)
    f.create_dataset("2d_enum_uint16_data", data=data, dtype=uint16_enum_type)
    uint32_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint32)
    f.create_dataset("2d_enum_uint32_data", data=data, dtype=uint32_enum_type)
    uint64_enum_type = h5py.enum_dtype({"RED": 0, "GREEN": 1, "BLUE": 2, "YELLOW": 3}, basetype=np.uint64)
    f.create_dataset("2d_enum_uint64_data", data=data, dtype=uint64_enum_type)

    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making compressed chunked dataset test files...')

    f = h5py.File('test_enum_datasets_earliest.hdf5', 'w', libver='earliest')
    write_enum_datasets(f)
    print('created test_enum_datasets_earliest.hdf5')

    f = h5py.File('test_enum_datasets_latest.hdf5', 'w', libver='latest')
    write_enum_datasets(f)
    print('created test_enum_datasets_latest.hdf5')
