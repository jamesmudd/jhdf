import h5py
import numpy as np

f = h5py.File('test_file.hdf5', 'w')

data = np.arange(-10, 11, 1)

datasets_group = f.create_group('datasets_group')

float_group = datasets_group.create_group('float')
float_group.create_dataset('float16', data=data, dtype='f2')
float_group.create_dataset('float32', data=data, dtype='f4')
float_group.create_dataset('float64', data=data, dtype='f8')

int_group = datasets_group.create_group('int')
int_group.create_dataset('int8', data=data, dtype='i1')
int_group.create_dataset('int16', data=data, dtype='i2')
int_group.create_dataset('int32', data=data, dtype='i4')

f.flush()
f.close()
