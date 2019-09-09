package io.jhdf.dataset;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.btree.Chunk;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singletonList;

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
        switch (layoutMessage.getIndexingType()) {
            case 1: // Single chunk
                logger.debug("Reading single chunk dataset");
                return singletonList(new SingleChunk(layoutMessage.getAddress()));
            default:
                throw new HdfException("Unreconized chunk indexing type = " + layoutMessage.getIndexingType());
        }
    }

    private class SingleChunk implements Chunk {

        private final long address;

        public SingleChunk(long address) {
            this.address = address;
        }

        @Override
        public int getSize() {
            int elementsInChunk = Arrays.stream(layoutMessage.getChunkDimensions()).reduce(1, Math::multiplyExact);
            return elementsInChunk * getDataType().getSize();
        }

        @Override
        public BitSet getFilterMask() {
            return null;
        }

        @Override
        public int[] getChunkOffset() {
            // Single chunk so there are no offsets
            return new int[getDimensions().length];
        }

        @Override
        public long getAddress() {
            return address;
        }
    }
}
