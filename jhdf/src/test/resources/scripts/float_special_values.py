#-------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
# 
# http://jhdf.io
# 
# Copyright (c) 2021 James Mudd
# 
# MIT License see 'LICENSE' file
#-------------------------------------------------------------------------------
import h5py

import numpy as np

'''
The idea of this test if to write datasets containg floating point special values
'''
def write_special_value_datasets(f):

    # dataset of special values
    data = np.array([np.inf, -np.inf, np.nan, np.PZERO, np.NZERO])

    f.create_dataset('float16', data=data, dtype='f2')
    f.create_dataset('float32', data=data, dtype='f4')
    f.create_dataset('float64', data=data, dtype='f8')

    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making special float value dataset test files...')

    f = h5py.File('float_special_values_earliest.hdf5', 'w', libver='earliest')
    write_special_value_datasets(f)
    print('created float_special_values_earliest.hdf5')

    f = h5py.File('float_special_values_latest.hdf5', 'w', libver='latest')
    write_special_value_datasets(f)
    print('created float_special_values_latest.hdf5')
