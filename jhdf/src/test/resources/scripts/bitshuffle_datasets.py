import h5py
import numpy
import bitshuffle.h5


f = h5py.File("bitshuffle_datasets.hdf5", "w",  libver='latest')

data = numpy.arange(20)

dtypes = ['int8', 'int16', 'float32', 'float64']
# 0 is auto blocksize
block_sizes = [0,8,64,1024,4096]
# 0 is no compression
# Need to  add bitshuffle.h5.H5_COMPRESS_ZSTD
compression = [0, bitshuffle.h5.H5_COMPRESS_LZ4]

for dtype in dtypes:
    for block_size in block_sizes:
        for comp in compression:
            f.create_dataset(
                dtype + "_bs" + str(block_size) + "_comp" + str(comp),
                data=data,
                compression=bitshuffle.h5.H5FILTER,
                compression_opts=(block_size, comp),
                dtype=dtype,
            )

f.close()
