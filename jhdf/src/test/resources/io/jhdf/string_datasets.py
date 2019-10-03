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


def write_string_datasets(f):
    
    # Fixed length (20) ASCII dataset
    fixed_length = 'S20'
    fixed_ds = f.create_dataset('fixed_length_ascii', (10,), dtype=fixed_length)
    for i in range(10):
        fixed_ds[i] = ('string number ' + str(i)).encode('ascii')
    
    # Fixed length (15) ASCII dataset the exact length of 'string number 0'
    fixed_length = 'S15'
    fixed_ds = f.create_dataset('fixed_length_ascii_1_char', (10,), dtype=fixed_length)
    for i in range(10):
        fixed_ds[i] = ('string number ' + str(i)).encode('ascii')

    # Variable length ASCII dataset
    ascii = h5py.special_dtype(vlen=bytes)
    varaible_ascii_ds = f.create_dataset('variable_length_ascii', (10,), dtype=ascii)
    for i in range(10):
        varaible_ascii_ds[i] = ('string number ' + str(i)).encode('ascii')
    
    # Variable length UTF8 dataset
    utf8 = h5py.special_dtype(vlen=str)
    varaible_ascii_ds = f.create_dataset('variable_length_utf8', (10,), dtype=utf8)
    for i in range(10):
        varaible_ascii_ds[i] = 'string number ' + str(i)
    
    
    # 2D utf8 data
    data = np.arange(35).reshape(5,7).astype(bytes)
    f.create_dataset('variable_length_2d', data=data, dtype=utf8)

    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making string dataset test files...')
    
    f = h5py.File('test_string_datasets_latest.hdf5', 'w', libver='latest')
    write_string_datasets(f)
    print('created test_string_datasets_latest.hdf5')
        
    f = h5py.File('test_string_datasets_earliest.hdf5', 'w', libver='earliest')
    write_string_datasets(f)
    print('created test_string_datasets_earliest.hdf5')
