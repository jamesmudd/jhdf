# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# https://jhdf.io
#
# Copyright (c) 2024 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py
import numpy

f = h5py.File("implicit_index_datasets.hdf5", "w",  libver='latest')

data = numpy.arange(20)

dataspace = h5py.h5s.create_simple(data.shape)  # Create simple dataspace
datatype = h5py.h5t.NATIVE_INT32

# Dataset creation property list
dcpl = h5py.h5p.create(h5py.h5p.DATASET_CREATE)
dcpl.set_alloc_time(h5py.h5d.ALLOC_TIME_EARLY)
# Set chunk dimensions (e.g., chunks of size 5)
chunk_dims = (5,)  # Ensure chunks are compatible with dataspace shape
dcpl.set_chunk(chunk_dims)

# Create the dataset
dataset_name = "implicit_index_exact".encode('utf-8')  # Dataset name must be bytes
dataset = h5py.h5d.create(f.id, dataset_name, datatype, dataspace, dcpl)

# Write data to the dataset
dataset.write(h5py.h5s.ALL, h5py.h5s.ALL, data)
dataset.close()

# Second dataset with chunk size mismatch
data = numpy.arange(50).reshape(10,5)

dataspace = h5py.h5s.create_simple(data.shape)  # Create simple dataspace
datatype = h5py.h5t.NATIVE_INT32

# Dataset creation property list
dcpl = h5py.h5p.create(h5py.h5p.DATASET_CREATE)
dcpl.set_alloc_time(h5py.h5d.ALLOC_TIME_EARLY)
# Set chunk dimensions
chunk_dims = (3,2) # mismatched to data shape
dcpl.set_chunk(chunk_dims)

# Create the dataset
dataset_name = "implicit_index_mismatch".encode('utf-8')  # Dataset name must be bytes
dataset = h5py.h5d.create(f.id, dataset_name, datatype, dataspace, dcpl)

# Write data to the dataset
dataset.write(h5py.h5s.ALL, h5py.h5s.ALL, data)
dataset.close()

f.close()
