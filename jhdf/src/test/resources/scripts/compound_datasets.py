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

# The idea of this test if to write compound datasets
def write_compound_datasets(f):

    utf8 = h5py.special_dtype(vlen=str)
    gender_enum_dtype = h5py.enum_dtype({"MALE": 0, "FEMALE": 1}, basetype=np.uint8)
    dt = np.dtype([
        ('firstName', utf8), # variable lentgh utf8
        ('surname', 'S20'), # fixed length ASCII
        ('gender', gender_enum_dtype), # enum type
        ('age', np.uint8), # uint
        ('fav_number', np.float32), # float
        ('vector', np.float32, (3,))]) # array

    data = np.zeros(4, dtype=dt)

    # Set the example data
    data[0] = ('Bob', 'Smith', 0, 32, 1.0, [1, 2, 3])
    data[1] = ('Peter', 'Fletcher', 0, 43, 2.0, [16.2, 2.2, -32.4])
    data[2] = ('James', 'Mudd', 0, 12, 3.0, [-32.1,-774.1,-3.0])
    data[3] = ('Ellie', 'Kyle', 1, 22, 4.0, [2.1,74.1,-3.8])

    f.create_dataset('contigious_compound', data=data)
    f.create_dataset('chunked_compound', data=data, chunks=(1,), compression="gzip")

    # 2d compound use img number example
    imgdt = np.dtype([
        ('real', np.float32),
        ('img', np.float32)
    ])
    data = np.zeros((3, 3), dtype=imgdt)
    data[0][0] = (2.3, -7.3)
    data[0][1] = (12.3, -17.3)
    data[0][2] = (-32.3, -0.3)
    data[1][0] = (2.3, -7.3)
    data[1][1] = (12.3, -17.3)
    data[1][2] = (-32.3, -0.3)
    data[2][0] = (2.3, -7.3)
    data[2][1] = (12.3, -17.3)
    data[2][2] = (-32.3, -0.3)

    f.create_dataset('2d_contigious_compound', data=data)
    f.create_dataset('2d_chunked_compound', data=data, chunks=(1,2), compression="gzip")

    # Compound dataset containing ragged arrays
    uint8_vlen_type = h5py.vlen_dtype(np.uint8)
    compound_vlen_dtype = np.dtype([
        ('one', uint8_vlen_type),
        ('two', uint8_vlen_type)
    ])
    data = np.zeros(3, dtype=compound_vlen_dtype)
    data[0] = (np.array([1]), np.array([2]))
    data[1] = (np.array([1,1]), np.array([2,2]))
    data[2] = (np.array([1,1,1]), np.array([2,2,2]))

    f.create_dataset('vlen_contigious_compound', data=data, dtype=compound_vlen_dtype)
    f.create_dataset('vlen_chunked_compound', data=data, dtype=compound_vlen_dtype, chunks=(1,), compression="gzip")

    # Compound dataset arrays of vlen type
    compound_vlen_dtype = np.dtype([
        ('name', utf8, 2)
    ])
    pointData = np.zeros(2, dtype=utf8)
    pointData[0] = "James"
    pointData[1] = "Ellie"
    data = np.zeros(1, dtype=compound_vlen_dtype)
    data['name'] = np.array(pointData)

    f.create_dataset('array_vlen_contigious_compound', data=data, dtype=compound_vlen_dtype)
    f.create_dataset('array_vlen_chunked_compound', data=data, dtype=compound_vlen_dtype, chunks=(1,), compression="gzip")

    f.flush()
    f.close()


if __name__ == '__main__':
    print('Making compound dataset test files...')

    f = h5py.File('compound_datasets_earliest.hdf5', 'w', libver='earliest')
    write_compound_datasets(f)
    print('created compound_datasets_earliest.hdf5')

    f = h5py.File('compound_datasets_latest.hdf5', 'w', libver='latest')
    write_compound_datasets(f)
    print('created compound_datasets_latest.hdf5')
