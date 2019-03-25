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

data=np.array([1])

def write_ordered_group(f):
    
    ordered_group = f.create_group('ordered_group', track_order=True)
    ordered_group.create_dataset("z", dtype='i4', data=data)
    ordered_group.create_dataset("h", dtype='i4', data=data)
    ordered_group.create_dataset("a", dtype='i4', data=data)

    # track_order=False is the default
    unordered_group = f.create_group('unordered_group', track_order=False)
    unordered_group.create_dataset("z", dtype='i4', data=data)
    unordered_group.create_dataset("h", dtype='i4', data=data)
    unordered_group.create_dataset("a", dtype='i4', data=data)
    
    f.flush()
    f.close()

if __name__ == '__main__':
    print('Making ordered group test files...')
    
    f = h5py.File('test_ordered_group_latest.hdf5', 'w', libver='latest')
    write_ordered_group(f)
    print('test_ordered_group_latest.hdf5')
