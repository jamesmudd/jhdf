# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# http://jhdf.io
#
# Copyright (c) 2023 James Mudd
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
    float_group.create_dataset('float32', data=data, dtype='f4', chunks=(2, 1), scaleoffset=0)
    float_group.create_dataset('float32_3', data=data, dtype='f4', chunks=(2, 1), scaleoffset=3)
    float_group.create_dataset('float64', data=data, dtype='f8', chunks=(3, 4), scaleoffset=0)
    float_group.create_dataset('float64_2', data=data, dtype='f8', chunks=(3, 4), scaleoffset=2)

    int_group = f.create_group('int')
    int_group.create_dataset('int8', data=data, dtype='i1', chunks=(5, 3), scaleoffset=0)
    int_group.create_dataset('int8_3', data=data, dtype='i1', chunks=(5, 3), scaleoffset=1)
    int_group.create_dataset('int16', data=data, dtype='i2', chunks=(1, 1), scaleoffset=0)
    int_group.create_dataset('int16_3', data=data, dtype='i2', chunks=(1, 1), scaleoffset=3)
    int_group.create_dataset('int32', data=data, dtype='i4', chunks=(1, 3), scaleoffset=0)
    int_group.create_dataset('int32_3', data=data, dtype='i4', chunks=(1, 3), scaleoffset=3)

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making compressed chunked dataset test files...')

    f = h5py.File('test_scale_offset_datasets_earliest.hdf5', 'w', libver='earliest')
    write_chunked_datasets(f)
    print('test_scale_offset_datasets_earliest.hdf5')

    f = h5py.File('test_scale_offset_datasets_latest.hdf5', 'w', libver='latest')
    write_chunked_datasets(f)
    print('test_scale_offset_datasets_latest.hdf5')
