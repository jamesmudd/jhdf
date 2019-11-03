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


def write_datasets(f):

    # Scalar data    
    data = np.float64(123.45) 
    
    # Float 64 - double
    f.create_dataset('scalar_float_64', data=data, dtype='f8')
    f.create_dataset("empty_float_64", data=h5py.Empty('f8'))
    
    # Float 32 - float
    f.create_dataset('scalar_float_32', data=data, dtype='f4')
    f.create_dataset("empty_float_32", data=h5py.Empty('f4'))
    
    
    data = np.int64(123)
    
    # Integer signed 64 - long
    f.create_dataset('scalar_int_64', data=data, dtype='i8')
    f.create_dataset("empty_int_64", data=h5py.Empty('i8'))
    
    # Integer signed 32 - int
    f.create_dataset('scalar_int_32', data=data, dtype='i4')
    f.create_dataset("empty_int_32", data=h5py.Empty('i4'))
    
    # Integer signed 16 - short
    f.create_dataset('scalar_int_16', data=data, dtype='i2')
    f.create_dataset("empty_int_16", data=h5py.Empty('i2'))
    
    # Integer signed 8 - byte
    f.create_dataset('scalar_int_8', data=data, dtype='i1')
    f.create_dataset("empty_int_8", data=h5py.Empty('i1'))
    
    # Integer unsigned 64 - BigInteger
    f.create_dataset('scalar_uint_64', data=data, dtype='u8')
    f.create_dataset("empty_uint_64", data=h5py.Empty('u8'))
    
    # Integer unsigned 32 - long
    f.create_dataset('scalar_uint_32', data=data, dtype='u4')
    f.create_dataset("empty_uint_32", data=h5py.Empty('u4'))
    
    # Integer unsigned 16 - int
    f.create_dataset('scalar_uint_16', data=data, dtype='u2')
    f.create_dataset("empty_uint_16", data=h5py.Empty('u2'))
    
    # Integer signed 8 - int
    f.create_dataset('scalar_uint_8', data=data, dtype='u1')
    f.create_dataset("empty_uint_8", data=h5py.Empty('u1'))
    
    # String
    data = np.str("hello")
    ascii = h5py.special_dtype(vlen=bytes)
    f.create_dataset('scalar_string', data=data, dtype=ascii)
    f.create_dataset("empty_string", data=h5py.Empty(ascii))
        
    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making scalar and empty dataset test files...')
    
    f = h5py.File('test_scalar_empty_datasets_latest.hdf5', 'w', libver='latest')
    write_datasets(f)
    print('created test_scalar_empty_datasets_latest.hdf5')
        
    f = h5py.File('test_scalar_empty_datasets_earliest.hdf5', 'w', libver='earliest')
    write_datasets(f)
    print('created test_scalar_empty_datasets_earliest.hdf5')
