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
The idea of this test is to write compressed byte shuffled chunked datasets
'''
def write_chunked_datasets(f):
    
    data = np.arange(35).reshape(7,5)

    float_group = f.create_group('float')
    float_group.create_dataset('float32', data=data, dtype='f4', chunks=(2,1), shuffle=True, compression="gzip")
    float_group.create_dataset('float64', data=data, dtype='f8', chunks=(3,4), shuffle=True, compression="gzip", compression_opts=9)
    
    int_group = f.create_group('int')
    int_group.create_dataset('int8', data=data, dtype='i1', chunks=(5,3), shuffle=True, compression="gzip")
    int_group.create_dataset('int16', data=data, dtype='i2', chunks=(1,1), shuffle=True, compression="gzip", compression_opts=1)
    int_group.create_dataset('int32', data=data, dtype='i4', chunks=(1,3), shuffle=True, compression="gzip", compression_opts=7)

    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making compressed byteshuffled chunked dataset test files...')

    f = h5py.File('test_byteshuffle_compressed_datasets_earliest.hdf5', 'w', libver='earliest')
    write_chunked_datasets(f)
    print('test_byteshuffle_compressed_datasets_earliest.hdf5')

    f = h5py.File('test_byteshuffle_compressed_datasets_latest.hdf5', 'w', libver='latest')
    write_chunked_datasets(f)
    print('test_byteshuffle_compressed_datasets_latest.hdf5')
