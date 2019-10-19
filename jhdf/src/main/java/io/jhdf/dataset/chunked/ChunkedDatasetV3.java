/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV1Data;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;

import java.util.Collection;

/**
 * This represents chunked datasets using a b-tree for indexing raw data chunks.
 * It supports filters for use when reading the dataset for example to
 * decompress.
 *
 * @author James Mudd
 */
public class ChunkedDatasetV3 extends ChunkedDatasetBase {

    private final ChunkedDataLayoutMessageV3 layoutMessage;

    public ChunkedDatasetV3(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
        super(hdfFc, address, name, parent, oh);

        layoutMessage = oh.getMessageOfType(ChunkedDataLayoutMessageV3.class);

    }

    @Override
    protected Collection<Chunk> getAllChunks() {
        final BTreeV1Data bTree = BTreeV1.createDataBTree(hdfFc, layoutMessage.getBTreeAddress(), getDimensions().length);
        return bTree.getChunks();
    }

    @Override
    protected int[] getChunkDimensions() {
        return layoutMessage.getChunkDimensions();
    }
}
