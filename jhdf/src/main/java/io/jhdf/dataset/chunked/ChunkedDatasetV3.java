/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import io.jhdf.Constants;
import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV1Data;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * This represents chunked datasets using a b-tree for indexing raw data chunks.
 * It supports filters for use when reading the dataset for example to
 * decompress.
 *
 * @author James Mudd
 */
public class ChunkedDatasetV3 extends ChunkedDatasetBase {
    private static final Logger logger = LoggerFactory.getLogger(ChunkedDatasetV3.class);

    private final ChunkedDataLayoutMessageV3 layoutMessage;

    private final ChunkLookupLazyInitializer chunkLookupLazyInitializer;

    public ChunkedDatasetV3(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
        super(hdfFc, address, name, parent, oh);

        layoutMessage = oh.getMessageOfType(ChunkedDataLayoutMessageV3.class);

        chunkLookupLazyInitializer = new ChunkLookupLazyInitializer();
    }

    @Override
    protected Map<ChunkOffset, Chunk> getChunkLookup() {
        try {
            return chunkLookupLazyInitializer.get();
        } catch (ConcurrentException e) {
            throw new HdfException("Failed to create chunk lookup for: " + getPath(), e);
        }
    }

    @Override
    public int[] getChunkDimensions() {
        return layoutMessage.getChunkDimensions();
    }

    private final class ChunkLookupLazyInitializer extends LazyInitializer<Map<ChunkOffset, Chunk>> {
        @Override
        protected Map<ChunkOffset, Chunk> initialize() {
            logger.debug("Creating chunk lookup for '{}'", getPath());

            if(layoutMessage.getBTreeAddress() == Constants.UNDEFINED_ADDRESS) {
                throw new HdfException("No storage allocated for '" + getPath() + "'");
            }

            final BTreeV1Data bTree = BTreeV1.createDataBTree(hdfFc, layoutMessage.getBTreeAddress(), getDimensions().length);
            final Collection<Chunk> allChunks = bTree.getChunks();

            return allChunks.stream().
                    collect(toMap(chunk -> new ChunkOffset(chunk.getChunkOffset()) // keys
                            , Function.identity())); // values
        }
    }
}
