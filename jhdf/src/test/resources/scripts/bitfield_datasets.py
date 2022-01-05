# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# http://jhdf.io
#
# Copyright (c) 2022 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------

import numpy as np
from tables import *

# Open a file in "w"rite mode
file = open_file("../hdf5/bitfield_datasets.hdf5", mode="w")

# Create the last table in group2
data = [bool(i % 2) for i in range(15)]
file.create_array("/", "bitfield", data)
file.create_carray("/", "chunked_bitfield", chunkshape=(2,), obj=data)
filters = Filters(complevel=1, complib='zlib', fletcher32=True)
file.create_carray("/", "compressed_chunked_bitfield", obj=data, chunkshape=(2,), filters=filters)

data2D = np.array(data).reshape(3, 5)
file.create_carray("/", "compressed_chunked_2d_bitfield", obj=data2D, chunkshape=(2, 3), filters=filters)

file.create_array("/", "scalar_bitfield", True)

file.close()
