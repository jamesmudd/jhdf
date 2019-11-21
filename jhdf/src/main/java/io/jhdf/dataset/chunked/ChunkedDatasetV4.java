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
import io.jhdf.btree.BTreeV2;
import io.jhdf.btree.record.FilteredDatasetChunks;
import io.jhdf.dataset.chunked.indexing.BTreeIndex;
import io.jhdf.dataset.chunked.indexing.ChunkIndex;
import io.jhdf.dataset.chunked.indexing.ExtensibleArrayIndex;
import io.jhdf.dataset.chunked.indexing.FixedArrayIndex;
import io.jhdf.dataset.chunked.indexing.SingleChunkIndex;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class ChunkedDatasetV4 extends ChunkedDatasetBase {
    private static final Logger logger = LoggerFactory.getLogger(ChunkedDatasetV4.class);

    private final ChunkedDataLayoutMessageV4 layoutMessage;

    public ChunkedDatasetV4(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
        super(hdfFc, address, name, parent, oh);

        layoutMessage = oh.getMessageOfType(ChunkedDataLayoutMessageV4.class);

        logger.debug("Created chunked v4 dataset. Index type {}", layoutMessage.getIndexingType());
    }

    @Override
    protected int[] getChunkDimensions() {
        // TODO understand why there is an extra one on the end of this array
        int[] chunkDimensions = layoutMessage.getChunkDimensions();
        return ArrayUtils.subarray(chunkDimensions, 0, chunkDimensions.length -1);
    }

    @Override
    protected Collection<Chunk> getAllChunks() {
        final DatasetInfo datasetInfo = new DatasetInfo(getChunkSizeInBytes(), getDimensions(), getChunkDimensions());
        final ChunkIndex chunkIndex;
        switch (layoutMessage.getIndexingType()) {
            case 1: // Single chunk
                logger.debug("Reading single chunk indexed dataset");
                chunkIndex = new SingleChunkIndex(layoutMessage, getChunkSizeInBytes(), getDimensions());
                break;
            case 2: // Implicit
                throw new UnsupportedHdfException("Implicit indexing is currently not supported");
            case 3: // Fixed array
                logger.debug("Reading fixed array indexed dataset");
                chunkIndex = new FixedArrayIndex(hdfFc, layoutMessage.getAddress(), getChunkSizeInBytes(), getDimensions(), getChunkDimensions());
                break;
            case 4: // Extensible Array
                logger.debug("Reading extensible array indexed dataset");
                chunkIndex = new ExtensibleArrayIndex(hdfFc, layoutMessage.getAddress(), getChunkSizeInBytes(), getDimensions(), getChunkDimensions());
                break;
            case 5: // B Tree V2
                logger.debug("Reading B tree v2 indexed dataset");
                chunkIndex = new BTreeIndex(hdfFc, layoutMessage.getAddress(), datasetInfo);
                break;
            default:
                throw new HdfException("Unrecognized chunk indexing type = " + layoutMessage.getIndexingType());
        }
        return chunkIndex.getAllChunks();
    }

    private int getChunkSizeInBytes() {
        return Arrays.stream(getChunkDimensions()).reduce(1, Math::multiplyExact) * getDataType().getSize();
    }

}
