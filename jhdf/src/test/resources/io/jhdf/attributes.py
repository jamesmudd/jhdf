import h5py

import numpy as np


def write_attribute_file(f):
    
    # Make group with attributes
    group = f.create_group('test_group')
    add_attributes(group)
    
    # Make dataset with attributes
    dataset = group.create_dataset('data', data=np.arange(5), dtype='f4')
    add_attributes(dataset)

    f.flush()
    f.close()


def add_attributes(node):
    # Integer
    node.attrs.create('scalar_int', np.int32(123), dtype='i4')
    node.attrs.create('1D_int', np.arange(10), dtype='i4')
    node.attrs.create('2D_int', np.arange(10).reshape(2,5), dtype='i4')
    node.attrs.create('empty_int', h5py.Empty('i4'), dtype='i4')
    
    # Float
    node.attrs.create('scalar_float', np.float32(123.45), dtype='f4')
    node.attrs.create('1D_float', np.arange(10), dtype='f4')
    node.attrs.create('2D_float', np.arange(10).reshape(2,5), dtype='f4')
    node.attrs.create('empty_float', h5py.Empty('f4'), dtype='f4')
    
    # String
    data = np.str("hello")
    ascii = h5py.special_dtype(vlen=bytes)
    node.attrs.create('scalar_string', data, dtype=ascii)
    node.attrs.create("empty_string", h5py.Empty(ascii))
    
    data = np.arange(6).reshape(2,3).astype(bytes)
    utf8 = h5py.special_dtype(vlen=str)
    node.attrs.create('2d_string', data=data, dtype=utf8)
    
    

if __name__ == '__main__':
    print('Making attribute test files...')
    
    f = h5py.File('test_attribute_latest.hdf5', 'w', libver='latest')
    write_attribute_file(f)
    print('created test_attribute_latest.hdf5')
        
    f = h5py.File('test_attribute_earliest.hdf5', 'w', libver='earliest')
    write_attribute_file(f)
    print('created test_attribute_earliest.hdf5')
