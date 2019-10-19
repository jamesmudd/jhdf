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

import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class FixedArrayIndex implements ChunkIndex {

    private static final byte[] FIXED_ARRAY_HEADER_SIGNATURE = "FAHD".getBytes();
    private static final byte[] FIXED_ARRAY_DATA_BLOCK_SIGNATURE = "FADB".getBytes();

    private final long address;
    private final int unfilteredChunkSize;
    private final int elementSize;
    private final int[] datasetDimensions;

    private final int clientId;
    private final int entrySize;
    private final int pageBits;
    private final int maxNumberOfEntries;
    private final long dataBlockAddress;

    private final List<Chunk> chunks;

    public FixedArrayIndex(HdfFileChannel hdfFc, long address, int unfilteredChunkSize, int elementSize, int[] datasetDimensions) {
        this.address = address;
        this.unfilteredChunkSize = unfilteredChunkSize;
        this.elementSize = elementSize;
        this.datasetDimensions = datasetDimensions;

        final int headerSize = 12 + hdfFc.getSizeOfOffsets() + hdfFc.getSizeOfLengths();
        final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

        byte[] formatSignatureBytes = new byte[4];
        bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);

        // Verify signature
        if (!Arrays.equals(FIXED_ARRAY_HEADER_SIGNATURE, formatSignatureBytes)) {
            throw new HdfException("Fixed array header signature 'FAHD' not matched, at address " + address);
        }

        // Version Number
        final byte version = bb.get();
        if (version != 0) {
            throw new HdfException("Unsupported fixed array index version detected. Version: " + version);
        }

        clientId = bb.get();
        entrySize = bb.get();
        pageBits = bb.get();

        maxNumberOfEntries = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        dataBlockAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());

        chunks = new ArrayList<>(maxNumberOfEntries);

        // Checksum

        // Building the object fills the chunks. Probably shoudld be changed
        new FixedArrayDataBlock(hdfFc, dataBlockAddress);
    }

    private class FixedArrayDataBlock {

        private FixedArrayDataBlock(HdfFileChannel hdfFc, long address) {

            // TODO header size ignoring paging
            final int headerSize = 6 + hdfFc.getSizeOfOffsets() + entrySize * maxNumberOfEntries;
            final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

            byte[] formatSignatureBytes = new byte[4];
            bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);

            // Verify signature
            if (!Arrays.equals(FIXED_ARRAY_DATA_BLOCK_SIGNATURE, formatSignatureBytes)) {
                throw new HdfException("Fixed array data block signature 'FADB' not matched, at address " + address);
            }

            // Version Number
            final byte version = bb.get();
            if (version != 0) {
                throw new HdfException("Unsupported fixed array data block version detected. Version: " + version);
            }

            final int clientId = bb.get();
            if (clientId != FixedArrayIndex.this.clientId) {
                throw new HdfException("Fixed array client ID mismatch. Possible file corruption detected");
            }

            final long headerAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
            if (headerAddress != FixedArrayIndex.this.address) {
                throw new HdfException("Fixed array data block header address missmatch");
            }

            // TODO ignoring paging here might need to revisit

            if (clientId == 0) { // Not filtered
                for (int i = 0; i < maxNumberOfEntries; i++) {
                    final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                    final int[] chunkOffset = Utils.linearIndexToDimensionIndex((i* unfilteredChunkSize)/elementSize, datasetDimensions);
                    chunks.add(new ChunkImpl(chunkAddress, unfilteredChunkSize, chunkOffset));
                }
            } else  if (clientId == 1) { // Filtered
                for (int i = 0; i < maxNumberOfEntries; i++) {
                    final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                    final int chunkSizeInBytes = Utils.readBytesAsUnsignedInt(bb, entrySize - hdfFc.getSizeOfOffsets() - 4);
                    final BitSet filterMask = BitSet.valueOf(new byte[] { bb.get(), bb.get(), bb.get(), bb.get() });
                    final int[] chunkOffset = Utils.linearIndexToDimensionIndex((i*unfilteredChunkSize)/elementSize, datasetDimensions);

                    chunks.add(new ChunkImpl(chunkAddress, chunkSizeInBytes, chunkOffset, filterMask));
                }
            } else {
                throw new HdfException("Unrecognized client ID  = " + clientId);
            }

        }
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return chunks;
    }
}
