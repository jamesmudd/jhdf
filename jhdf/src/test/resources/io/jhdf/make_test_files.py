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

data = np.arange(-10, 11, 1)
data_3d = np.arange(1000).reshape((2,5,100)) 

def write_to_file(f, data):
    datasets_group = f.create_group('datasets_group')
    
    datasets_group.attrs['string_attr'] = 'my string attribute'
    datasets_group.attrs['int_attr'] = 123
    datasets_group.attrs['float_attr'] = 123.456
    
    float_group = datasets_group.create_group('float')
    float_group.create_dataset('float32', data=data, dtype='f4')
    float_group.create_dataset('float64', data=data, dtype='f8', fillvalue=6)
    
    int_group = datasets_group.create_group('int')
    int_group.create_dataset('int8', data=data, dtype='i1')
    int_group.create_dataset('int16', data=data, dtype='i2')
    int_group.create_dataset('int32', data=data, dtype='i4')
    
    links_group = f.create_group('links_group')
    links_group['hard_link_to_int8'] = int_group['int8']
    links_group['soft_link_to_int8'] = h5py.SoftLink('/datasets_group/int/int8')
    links_group['broken_soft_link'] = h5py.SoftLink('/datasets_group/int/missing_dataset')
    links_group['soft_link_to_group'] = h5py.SoftLink('/datasets_group/int')
    # Define the external link path relative to this file, to ease testing
    links_group['external_link'] = h5py.ExternalLink('test_file_ext.hdf5', '/external_dataset')
    links_group['external_link_to_missing_file'] = h5py.ExternalLink('missing_file.hdf5', '/external_dataset')
    
    multiDimensionDatasets = f.create_group('nD_Datasets');
    multiDimensionDatasets.create_dataset('3D_float32', data=data_3d, dtype='f4')
    multiDimensionDatasets.create_dataset('3D_int32', data=data_3d, dtype='i4')
    
    f.flush()
    f.close()
    

if __name__ == '__main__':
    print('Making test files...')
    
    #     
    f = h5py.File('test_file.hdf5', 'w', libver='earliest')
    write_to_file(f, data)
    print('created test_file.hdf5')
    
    f2 = h5py.File('test_file2.hdf5', 'w', libver='latest')
    write_to_file(f2, data)
    print('created test_file2.hdf5')
    
    f3 = h5py.File('test_file_ext.hdf5', 'w', libver='latest')
    f3.create_dataset('external_dataset', data=data, dtype='f4')
    print('test_file_ext.hdf5')
    
    print('Created all test files')
