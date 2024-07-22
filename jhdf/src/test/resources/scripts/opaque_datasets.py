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

'''
The idea of this test if to write datasets containing opaque datasets
'''


def write_datasets(f):
    # dataset of special values
    data = np.array([
        np.datetime64('2017-02-22T14:14:14'),
        np.datetime64('2018-02-22T14:14:14'),
        np.datetime64('2019-02-22T14:14:14'),
        np.datetime64('2020-02-22T14:14:14'),
        np.datetime64('2021-02-22T14:14:14'),
    ])

    dataType = h5py.opaque_dtype(data.dtype)
    f.create_dataset('timestamp', data=data.astype(dataType))

    # 2D String data
    data = np.arange(35).reshape(5, 7).astype(bytes)
    dataType = h5py.opaque_dtype(data.dtype)
    f.create_dataset('opaque_2d_string', data=data.astype(dataType))

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making opaque dataset test files...')

    f = h5py.File('opaque_datasets_earliest.hdf5', 'w', libver='earliest')
    write_datasets(f)
    print('created opaque_datasets_earliest.hdf5')

    f = h5py.File('opaque_datasets_latest.hdf5', 'w', libver='latest')
    write_datasets(f)
    print('created opaque_datasets_latest.hdf5')
