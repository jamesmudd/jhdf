package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

public class SingleChunkIndex implements ChunkIndex {

    private final Chunk singleChunk;

    public SingleChunkIndex(long chunkAddress, int chunkSize, int[] dimensions) {
        final int[] chunkOffset = new int[dimensions.length]; // Single chunk so zero offset
        final BitSet filterMask = BitSet.valueOf(new byte[]{}); // No filters so no mask
        this.singleChunk = new SingleChunk(chunkAddress, chunkSize, chunkOffset, filterMask);
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return Collections.singletonList(singleChunk);
    }

    private class SingleChunk implements Chunk {
        private final long address;
        private final int size;
        private final int[] chunkOffset;
        private final BitSet filterMask;

        private SingleChunk(long address, int size, int[] chunkOffset, BitSet filterMask) {
            this.address = address;
            this.size = size;
            this.chunkOffset = chunkOffset;
            this.filterMask = filterMask;
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public BitSet getFilterMask() {
            return filterMask;
        }

        @Override
        public int[] getChunkOffset() { return chunkOffset; }

        @Override
        public long getAddress() {
            return address;
        }
    }
}
