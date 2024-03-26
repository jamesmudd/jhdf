import h5py
import numpy as np

f = h5py.File('webinar_demo.hdf5', 'w', libver='earliest')

simpleData = np.arange(10)
f.create_dataset('ints', data=simpleData, dtype='i4')

utf8 = h5py.special_dtype(vlen=str)
gender_enum_dtype = h5py.enum_dtype({"MALE": 0, "FEMALE": 1}, basetype=np.uint8)
dt = np.dtype([
    ('firstName', utf8),  # variable length utf8
    ('surname', 'S20'),  # fixed length ASCII
    ('gender', gender_enum_dtype),  # enum type
    ('age', np.uint8),  # uintf
    ('fav_number', np.float32),  # float
    ('vector', np.float32, (3,))])  # array

data = np.zeros(4, dtype=dt)

# Set the example data
data[0] = ('Bob', 'Smith', 0, 32, 1.0, [1, 2, 3])
data[1] = ('Peter', 'Fletcher', 0, 43, 2.0, [16.2, 2.2, -32.4])
data[2] = ('James', 'Mudd', 0, 12, 3.0, [-32.1, -774.1, -3.0])
data[3] = ('Ellie', 'Kyle', 1, 22, 4.0, [2.1, 74.1, -3.8])

f.create_dataset('people', data=data)

data_2d_group = f.create_group('2d_data')
data2d = np.arange(350).reshape(7, 50)
data_2d_group.create_dataset('2d_data', data=data2d, dtype='f8')
data_2d_group.create_dataset('byteshuffle', data=data2d, dtype='f8', chunks=(3, 4), shuffle=True, compression="gzip")

f.flush()
f.close()
