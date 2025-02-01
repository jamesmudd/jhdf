# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# https://jhdf.io
#
# Copyright (c) 2025 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py

import numpy as np

'''
The idea of this test is to write compact datasets
'''


def write_chunked_datasets(f):
    data = np.arange(10)

    compact = h5py.h5p.create(h5py.h5p.DATASET_CREATE)
    compact.set_layout(h5py.h5d.COMPACT)

    float_group = f.create_group('float')
    float_group.create_dataset('float16', data=data, dtype='f2', dcpl=compact)
    float_group.create_dataset('float32', data=data, dtype='f4', dcpl=compact)
    float_group.create_dataset('float64', data=data, dtype='f8', dcpl=compact)

    int_group = f.create_group('int')
    int_group.create_dataset('int8', data=data, dtype='i1', dcpl=compact)
    int_group.create_dataset('int16', data=data, dtype='i2', dcpl=compact)
    int_group.create_dataset('int32', data=data, dtype='i4', dcpl=compact)

    string_group = f.create_group('string')
    # Fixed length (20) ASCII dataset
    fixed_length = 'S20'
    fixed_ds = string_group.create_dataset('fixed_length_ascii', (10,), dtype=fixed_length, dcpl=compact)
    for i in range(10):
        fixed_ds[i] = ('string number ' + str(i)).encode('ascii')

    # Fixed length (15) ASCII dataset the exact length of 'string number 0'
    fixed_length = 'S15'
    fixed_ds = string_group.create_dataset('fixed_length_ascii_1_char', (10,), dtype=fixed_length, dcpl=compact)
    for i in range(10):
        fixed_ds[i] = ('string number ' + str(i)).encode('ascii')

    # Variable length ASCII dataset
    ascii = h5py.special_dtype(vlen=bytes)
    varaible_ascii_ds = string_group.create_dataset('variable_length_ascii', (10,), dtype=ascii, dcpl=compact)
    for i in range(10):
        varaible_ascii_ds[i] = ('string number ' + str(i)).encode('ascii')

    # Variable length UTF8 dataset
    utf8 = h5py.special_dtype(vlen=str)
    varaible_ascii_ds = string_group.create_dataset('variable_length_utf8', (10,), dtype=utf8, dcpl=compact)
    for i in range(10):
        varaible_ascii_ds[i] = 'string number ' + str(i)

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making compact dataset test files...')

    f = h5py.File('test_compact_datasets_earliest.hdf5', 'w', libver='earliest')
    write_chunked_datasets(f)
    print('created test_compact_datasets_earliest.hdf5')

    f = h5py.File('test_compact_datasets_latest.hdf5', 'w', libver='latest')
    write_chunked_datasets(f)
    print('created test_compact_datasets_latest.hdf5')
