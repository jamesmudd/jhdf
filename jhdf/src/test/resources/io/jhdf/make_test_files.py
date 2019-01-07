import h5py
import numpy as np

data = np.arange(-10, 11, 1)

def write_to_file(f, data):
    datasets_group = f.create_group('datasets_group')
    
    datasets_group.attrs['string_attr'] = 'my string attribute'
    datasets_group.attrs['int_attr'] = 123
    datasets_group.attrs['float_attr'] = 123.456
    
    float_group = datasets_group.create_group('float')
    float_group.create_dataset('float16', data=data, dtype='f2')
    float_group.create_dataset('float32', data=data, dtype='f4')
    float_group.create_dataset('float64', data=data, dtype='f8', fillvalue=6)
    
    int_group = datasets_group.create_group('int')
    int_group.create_dataset('int8', data=data, dtype='i1')
    int_group.create_dataset('int16', data=data, dtype='i2')
    int_group.create_dataset('int32', data=data, dtype='i4')
    
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
    
    print('Created all test files')
