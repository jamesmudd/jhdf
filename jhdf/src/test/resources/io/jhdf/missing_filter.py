# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
# 
# http://jhdf.io
# 
# Copyright 2019 James Mudd
# 
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py

import numpy as np

'''
The idea of this test is to write a dataset with a missing filter
'''


def write_chunked_datasets(f):
    data = np.arange(35).reshape(7, 5)

    # Compress with lzf which jhdf doesn't include by default
    f.create_dataset('float32', data=data, dtype='f4', chunks=(2, 1), shuffle=True, compression="lzf")

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making missing filter test file...')

    f = h5py.File('test_missing_filter.hdf5bad', 'w', libver='earliest')
    write_chunked_datasets(f)
    print('test_missing_filter.hdf5bad')
