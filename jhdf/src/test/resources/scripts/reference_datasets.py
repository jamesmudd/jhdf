# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# http://jhdf.io
#
# Copyright (c) 2021 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py
import numpy as np

data = np.arange(-10, 11, 1)

def write_to_file(f, data):
    datasets_group = f.create_group('datasets_group')

    # Datasets to be referenced
    float32 = datasets_group.create_dataset('float32', data=data, dtype='f4')
    float64 = datasets_group.create_dataset('float64', data=data, dtype='f8', fillvalue=6)
    int8 = datasets_group.create_dataset('int8', data=data, dtype='i1')
    int16 = datasets_group.create_dataset('int16', data=data, dtype='i2')
    int32 = datasets_group.create_dataset('int32', data=data, dtype='i4')

    # Dataset of references
    ref_dataset = f.create_dataset("references", (6,), dtype=h5py.ref_dtype)
    ref_dataset[0] = float32.ref
    ref_dataset[1] = float64.ref
    ref_dataset[2] = int8.ref
    ref_dataset[3] = int16.ref
    ref_dataset[4] = int32.ref
    ref_dataset[5] = datasets_group.ref

    # Add reference attributes
    ref_dataset.attrs.create("floatAttr", float32.ref, dtype=h5py.ref_dtype)
    ref_dataset.attrs.create("intAttr", int32.ref, dtype=h5py.ref_dtype)

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making reference test files...')

    #
    f = h5py.File('reference_datasets_earliest.hdf5', 'w', libver='earliest')
    write_to_file(f, data)
    print('created reference_datasets_earliest.hdf5')

    f2 = h5py.File('reference_datasets_latest.hdf5', 'w', libver='latest')
    write_to_file(f2, data)
    print('reference_datasets_latest.hdf5')

    print('Making reference test files...')
