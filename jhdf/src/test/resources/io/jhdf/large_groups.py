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


def write_large_group(f):
    large_group = f.create_group('large_group')
    for i in range(1000):
        large_group.create_dataset("data" + str(i), dtype='i4', data=np.array([i]))
    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making large group test files...')
    
    f = h5py.File('test_large_group_latest.hdf5', 'w', libver='latest')
    write_large_group(f)
    print('created test_long_file_latest.hdf5')
        
    f = h5py.File('test_large_group_earliest.hdf5', 'w', libver='earliest')
    write_large_group(f)
    print('created test_long_file_earliest.hdf5')
