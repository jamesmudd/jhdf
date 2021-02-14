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
import io.jhdf.dataset.chunked.indexing.BTreeIndex;
import io.jhdf.dataset.chunked.indexing.ChunkIndex;
import io.jhdf.dataset.chunked.indexing.EmptyChunkIndex;
import io.jhdf.dataset.chunked.indexing.ExtensibleArrayIndex;
import io.jhdf.dataset.chunked.indexing.FixedArrayIndex;
import io.jhdf.dataset.chunked.indexing.SingleChunkIndex;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class ChunkedDatasetV4 extends ChunkedDatasetBase {
    private static final Logger logger = LoggerFactory.getLogger(ChunkedDatasetV4.class);

    private final ChunkedDataLayoutMessageV4 layoutMessage;
    private final ChunkLookupLazyInitializer chunkLookupLazyInitializer;

    public ChunkedDatasetV4(HdfBackingStorage hdfBackingStorage, long address, String name, Group parent, ObjectHeader oh) {
        super(hdfBackingStorage, address, name, parent, oh);

        layoutMessage = oh.getMessageOfType(ChunkedDataLayoutMessageV4.class);
        chunkLookupLazyInitializer = new ChunkLookupLazyInitializer();

        logger.debug("Created chunked v4 dataset. Index type {}", layoutMessage.getIndexingType());
    }

    @Override
    public int[] getChunkDimensions() {
        // TODO understand why there is an extra one on the end of this array
        int[] chunkDimensions = layoutMessage.getChunkDimensions();
        return ArrayUtils.subarray(chunkDimensions, 0, chunkDimensions.length -1);
    }

    @Override
    protected Map<ChunkOffset, Chunk> getChunkLookup() {
        try {
            return chunkLookupLazyInitializer.get();
        } catch (ConcurrentException e) {
            throw new HdfException("Failed to create chunk lookup for: " + getPath(), e);
        }
    }

    private final class ChunkLookupLazyInitializer extends LazyInitializer<Map<ChunkOffset, Chunk>> {
        @Override
        protected Map<ChunkOffset, Chunk> initialize() {
            logger.debug("Creating chunk lookup for '{}'", getPath());

            final DatasetInfo datasetInfo = new DatasetInfo(getChunkSizeInBytes(), getDimensions(), getChunkDimensions());
            final ChunkIndex chunkIndex;

            if(layoutMessage.getAddress() == Constants.UNDEFINED_ADDRESS) {
                logger.debug("No storage allocated for '{}'", getPath());
                chunkIndex = new EmptyChunkIndex();
            } else {

                switch (layoutMessage.getIndexingType()) {
                    case 1: // Single chunk
                        logger.debug("Reading single chunk indexed dataset");
                        chunkIndex = new SingleChunkIndex(layoutMessage, datasetInfo);
                        break;
                    case 2: // Implicit
                        throw new UnsupportedHdfException("Implicit indexing is currently not supported");
                    case 3: // Fixed array
                        logger.debug("Reading fixed array indexed dataset");
                        chunkIndex = new FixedArrayIndex(hdfBackingStorage, layoutMessage.getAddress(), datasetInfo);
                        break;
                    case 4: // Extensible Array
                        logger.debug("Reading extensible array indexed dataset");
                        chunkIndex = new ExtensibleArrayIndex(hdfBackingStorage, layoutMessage.getAddress(), datasetInfo);
                        break;
                    case 5: // B Tree V2
                        logger.debug("Reading B tree v2 indexed dataset");
                        chunkIndex = new BTreeIndex(hdfBackingStorage, layoutMessage.getAddress(), datasetInfo);
                        break;
                    default:
                        throw new HdfException("Unrecognized chunk indexing type = " + layoutMessage.getIndexingType());
                }
            }

            final Collection<Chunk> allChunks = chunkIndex.getAllChunks();

            return allChunks.stream().
                    collect(toMap(chunk -> new ChunkOffset(chunk.getChunkOffset()) // keys
                            , Function.identity())); // values
        }

        private int getChunkSizeInBytes() {
            return Arrays.stream(getChunkDimensions()).reduce(1, Math::multiplyExact) * getDataType().getSize();
        }

    }

}
