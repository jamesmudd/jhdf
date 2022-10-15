import h5py
import numpy
import hdf5plugin

f = h5py.File("lz4_datasets.hdf5", "w",  libver='latest')

data = numpy.arange(20)

dtypes = ['int8', 'int16', 'float32', 'float64']
# 0 is auto blocksize
block_sizes = [0, 8, 64, 1024, 4096]

for dtype in dtypes:
    for block_size in block_sizes:
        # for comp in compression:
        f.create_dataset(
            dtype + "_bs" + str(block_size),
            data=data,
            **hdf5plugin.LZ4(nbytes=block_size),
            dtype=dtype
        )

f.close()
