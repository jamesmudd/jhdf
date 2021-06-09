# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# http://jhdf.io
#
# Copyright (c) 2021 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py

import numpy as np

'''
The idea of this test if to write compressed chunked datasets
'''


def write_chunked_datasets(f):
    data = np.arange(35).reshape(7, 5)

    float_group = f.create_group('float')
    float_group.create_dataset('float32', data=data, dtype='f4', chunks=(2, 1), compression="gzip")
    float_group.create_dataset('float32lzf', data=data, dtype='f4', chunks=(2, 1), compression="lzf")
    float_group.create_dataset('float64', data=data, dtype='f8', chunks=(3, 4), compression="gzip", compression_opts=9)
    float_group.create_dataset('float64lzf', data=data, dtype='f8', chunks=(3, 4), compression="lzf")

    int_group = f.create_group('int')
    int_group.create_dataset('int8', data=data, dtype='i1', chunks=(5, 3), compression="gzip")
    int_group.create_dataset('int8lzf', data=data, dtype='i1', chunks=(5, 3), compression="lzf")
    int_group.create_dataset('int16', data=data, dtype='i2', chunks=(1, 1), compression="gzip", compression_opts=1)
    int_group.create_dataset('int16lzf', data=data, dtype='i2', chunks=(1, 1), compression="lzf")
    int_group.create_dataset('int32', data=data, dtype='i4', chunks=(1, 3), compression="gzip", compression_opts=7)
    int_group.create_dataset('int32lzf', data=data, dtype='i4', chunks=(1, 3), compression="lzf")

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making compressed chunked dataset test files...')

    f = h5py.File('test_compressed_chunked_datasets_earliest.hdf5', 'w', libver='earliest')
    write_chunked_datasets(f)
    print('test_compressed_chunked_datasets_earliest.hdf5')

    f = h5py.File('test_compressed_chunked_datasets_latest.hdf5', 'w', libver='latest')
    write_chunked_datasets(f)
    print('test_compressed_chunked_datasets_latest.hdf5')
