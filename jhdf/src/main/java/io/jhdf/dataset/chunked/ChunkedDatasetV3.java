/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import io.jhdf.Constants;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV1Data;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
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

    private final ChunkedDataLayoutMessage layoutMessage;

    private final ChunkLookupLazyInitializer chunkLookupLazyInitializer;

    public ChunkedDatasetV3(HdfBackingStorage hdfBackingStorage, long address, String name, Group parent, ObjectHeader oh) {
        super(hdfBackingStorage, address, name, parent, oh);

        layoutMessage = oh.getMessageOfType(ChunkedDataLayoutMessage.class);

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
            logger.debug("Creating chunk lookup for [{}]", getPath());

            if(layoutMessage.getBTreeAddress() == Constants.UNDEFINED_ADDRESS) {
                return Collections.emptyMap();
            }

            final BTreeV1Data bTree = BTreeV1.createDataBTree(hdfBackingStorage, layoutMessage.getBTreeAddress(), getDimensions().length);
            final Collection<Chunk> allChunks = bTree.getChunks();

            return allChunks.stream().
                    collect(toMap(chunk -> new ChunkOffset(chunk.getChunkOffset()) // keys
                            , Function.identity())); // values
        }
    }

}
