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
The idea of this test if to write datasets with fill values
'''
def write_datasets(f):
    
    data = np.arange(10).reshape(2,5)

    float_group = f.create_group('float')
    float_group.create_dataset('float32', dtype='f4', data=data, fillvalue=33.33, shape=(2,5))
    float_group.create_dataset('float64', dtype='f8', data=data, fillvalue=123.456, shape=(2,5))
    
    int_group = f.create_group('int')
    int_group.create_dataset('int8', dtype='i1', data=data, fillvalue=8, shape=(2,5))
    int_group.create_dataset('int16', dtype='i2', data=data, fillvalue=16, shape=(2,5))
    int_group.create_dataset('int32', dtype='i4', data=data, fillvalue=32, shape=(2,5))

    f.create_dataset('no_fill', dtype='i1', data=data, shape=(2,5))

    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making fill value test files...')

    f = h5py.File('test_fill_value_earliest.hdf5', 'w', libver='earliest')
    write_datasets(f)
    print('test_fill_value_earliest.hdf5')

    f = h5py.File('test_fill_value_latest.hdf5', 'w', libver='latest')
    write_datasets(f)
    print('created test_fill_value_latest.hdf5')
    
