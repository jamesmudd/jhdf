import numpy as np
import h5py

if __name__ == '__main__':
    print('Making large attribute test files...')

    # Can only work with latest
    f = h5py.File('large_attribute.hdf5', 'w', libver='latest')

    # Small test dataset with large attributes
    data = np.arange(5)
    dataset = f.create_dataset('data', data=data, dtype='i1')

    # Large attributes >64kB
    large_attr_data = np.arange(8200, dtype=np.float64)
    f.attrs.create('large_attribute', data=large_attr_data, dtype='f8')

    print('created large_attribute.hdf5')

