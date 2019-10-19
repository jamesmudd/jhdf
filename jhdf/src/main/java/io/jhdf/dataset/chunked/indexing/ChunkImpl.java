/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public class ChunkImpl implements Chunk {

    private static final BitSet NOT_FILTERED_MASK = BitSet.valueOf(new byte[4]); // No filter mask so just all off

    private final long address;
    private final int size;
    private final int[] chunkOffset;
    private final BitSet filterMask;

    public ChunkImpl(long address, int size, int[] chunkOffset) {
        this(address, size, chunkOffset, NOT_FILTERED_MASK);
    }

    public ChunkImpl(long address, int size, int[] chunkOffset, BitSet filterMask) {
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
    public int[] getChunkOffset() {
        return chunkOffset;
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkImpl chunk = (ChunkImpl) o;
        return address == chunk.address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "ChunkImpl{" +
                "address=" + address +
                ", size=" + size +
                ", chunkOffset=" + Arrays.toString(chunkOffset) +
                ", filterMask=" + filterMask +
                '}';
    }
}
