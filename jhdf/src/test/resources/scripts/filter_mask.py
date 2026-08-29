# -------------------------------------------------------------------------------
# This file is part of jHDF. A pure Java library for accessing HDF5 files.
#
# https://jhdf.io
#
# Copyright (c) 2026 James Mudd
#
# MIT License see 'LICENSE' file
# -------------------------------------------------------------------------------
import h5py
import numpy as np


if __name__ == '__main__':
    print('Making filter mask dataset test file...')

    data = np.arange(8, dtype=np.int8)
    with h5py.File('filter_mask.hdf5', 'w', libver='latest') as f:
        dataset = f.create_dataset('raw_chunk', shape=data.shape, dtype=data.dtype,
                                   chunks=data.shape, compression='gzip')
        dataset.id.write_direct_chunk((0,), data.tobytes(), filter_mask=1)

        assert dataset.id.get_chunk_info(0).filter_mask == 1

    print('filter_mask.hdf5')
