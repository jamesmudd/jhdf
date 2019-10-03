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

'''
The idea of this test if to write userblock data to HDF5 file
'''
def write_userblock_data(f):
    
    filename = f.filename
    # Close the h5py file
    f.close()
    
    # Open as plain binary file to write the user block
    with open(filename, "rb+") as fb:
        fb.write("userblock data here...".encode(encoding='utf_8', errors='strict'))


if __name__ == '__main__':
    print('Making userblock test files...')

    f = h5py.File('test_userblock_earliest.hdf5', 'w', userblock_size=512, libver='earliest')
    write_userblock_data(f)
    print('created test_userblock_earliest.hdf5')

    f = h5py.File('test_userblock_latest.hdf5', 'w', userblock_size=1024, libver='latest')
    write_userblock_data(f)
    print('created test_userblock_latest.hdf5')
