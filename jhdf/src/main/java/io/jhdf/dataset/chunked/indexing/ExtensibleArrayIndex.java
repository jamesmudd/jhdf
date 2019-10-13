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
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static io.jhdf.Utils.readBytesAsUnsignedLong;

/**
 * Extensible Array Chunk Index
 * <p>
 * This was added to allow for more efficient indexing of chunks for datasets with only one unlimited dimension.
 * <p>
 * More info on how extensible arrays work can be found at https://bitbucket.hdfgroup.org/projects/HDFFV/repos/hdf5doc/browse/projects/1_10_alpha/ReviseChunks/skip_lists
 * </p>
 * https://support.hdfgroup.org/HDF5/doc/H5.format.html#ExtensibleArray
 *
 * @author James Mudd
 */
public class ExtensibleArrayIndex implements ChunkIndex {

    private static final byte[] EXTENSIBLE_ARRAY_HEADER_SIGNATURE = "EAHD".getBytes();
    private static final byte[] EXTENSIBLE_ARRAY_INDEX_BLOCK_SIGNATURE = "EAIB".getBytes();
    private static final byte[] EXTENSIBLE_ARRAY_DATA_BLOCK_SIGNATURE = "EADB".getBytes();
    private static final byte[] EXTENSIBLE_ARRAY_SECONDARY_BLOCK_SIGNATURE = "EASB".getBytes();

    private final long headerAddress;
    private final int clientId;
    private final boolean filtered; // If the chunks have filters applied
    private final int elementSize;
    private final int numberOfElementsInIndexBlock;
    private final int numberOfElements;
    private final int numberOfSecondaryBlocks;
    private final int blockOffsetSize;
    private final int dataBlockSize;
    private final int secondaryBlockSize;

    private final List<Chunk> chunks;
    private final int unfilteredChunkSize;
    private final int[] datasetDimensions;
    private final int minNumberOfElementsInDataBlock;
    private final ExtensibleArrayCounter extensibleArrayCounter;
    private final int minNumberOfDataBlockPointers;
    private final int maxNumberOfElementsInDataBlockPageBits;
    private final int extensibleArrayElementSize;

    private int elementCounter = 0;

    public ExtensibleArrayIndex(HdfFileChannel hdfFc, long address, int chunkSizeInBytes, int elementSize, int[] dimensions) {
        this.headerAddress = address;
        this.unfilteredChunkSize = chunkSizeInBytes;
        this.elementSize = elementSize;
        this.datasetDimensions = dimensions;

        final int headerSize = 16 + hdfFc.getSizeOfOffsets() + 6 * hdfFc.getSizeOfLengths();
        final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

        verifySignature(bb, EXTENSIBLE_ARRAY_HEADER_SIGNATURE);

        // Version Number
        final byte version = bb.get();
        if (version != 0) {
            throw new HdfException("Unsupported extensible array index version detected. Version: " + version);
        }

        clientId = bb.get();
        if (clientId == 0) {
            filtered = false;
        } else if (clientId == 1) {
            filtered = true;
        } else {
            throw new UnsupportedHdfException("Extensible array unsupported client ID: " + clientId);
        }

        extensibleArrayElementSize = bb.get();

        final int maxNumberOfElementsBits = bb.get();
        blockOffsetSize = maxNumberOfElementsBits / 8; // TODO round up?
        numberOfElementsInIndexBlock = bb.get();
        minNumberOfElementsInDataBlock = bb.get();
        extensibleArrayCounter = new ExtensibleArrayCounter(minNumberOfElementsInDataBlock);
        minNumberOfDataBlockPointers = bb.get();
        maxNumberOfElementsInDataBlockPageBits = bb.get();

        numberOfSecondaryBlocks = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        secondaryBlockSize = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        final int numberOfDataBlocks = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        dataBlockSize = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        final int maxIndexSet = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        chunks = new ArrayList<>(maxIndexSet);

        numberOfElements = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        final int indexBlockAddress = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        new ExtensibleArrayIndexBlock(hdfFc, indexBlockAddress);

        // Checksum
    }

    private class ExtensibleArrayIndexBlock {

        private ExtensibleArrayIndexBlock(HdfFileChannel hdfFc, long address) {

            // Figure out the size of the index block
            final int headerSize = 6 + hdfFc.getSizeOfOffsets()
                    // TODO need to handle filtered elements
                    + hdfFc.getSizeOfOffsets() * numberOfElementsInIndexBlock // direct chunk pointers
                    + 6 * extensibleArrayElementSize // Always up to 6 data block pointers are in the index block
                    + numberOfSecondaryBlocks * hdfFc.getSizeOfOffsets() // Secondary block addresses.
                    + 4; // checksum


            final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

            verifySignature(bb, EXTENSIBLE_ARRAY_INDEX_BLOCK_SIGNATURE);

            // Version Number
            final byte version = bb.get();
            if (version != 0) {
                throw new HdfException("Unsupported fixed array data block version detected. Version: " + version);
            }

            final int clientId = bb.get();
            if (clientId != ExtensibleArrayIndex.this.clientId) {
                throw new HdfException("Extensible array client ID mismatch. Possible file corruption detected");
            }

            final long headerAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
            if (headerAddress != ExtensibleArrayIndex.this.headerAddress) {
                throw new HdfException("Extensible array data block header address missmatch");
            }

            // Elements in Index block
            for (int i = 0; i < numberOfElementsInIndexBlock; i++) {
                readElement(bb, hdfFc);
            }

            // Guard against all the elements having already been read
            if (numberOfElements > numberOfElementsInIndexBlock) {
                // Upto 6 data block pointers directly in the index block
                for (int i = 0; i < 6; i++) {
                    final long dataBlockAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                    if (dataBlockAddress == UNDEFINED_ADDRESS) {
                        break; // There was less than 6 data blocks for the full dataset
                    }
                    new ExtensibleArrayDataBlock(hdfFc, dataBlockAddress);
                }
            }

            // Now read secondary blocks
            for (int i = 0; i < numberOfSecondaryBlocks; i++) {
                final long secondaryBlockAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                if (secondaryBlockAddress == UNDEFINED_ADDRESS) {
                    break; // There was less than 6 data blocks for the full dataset
                }
                new ExtensibleArraySecondaryBlock(hdfFc, secondaryBlockAddress);
            }

            // Checksum

        }

        private class ExtensibleArrayDataBlock {

            private ExtensibleArrayDataBlock(HdfFileChannel hdfFc, long address) {

                final int numberOfElements = extensibleArrayCounter.getNextNumberOfChunks();
                final int headerSize = 6 + hdfFc.getSizeOfOffsets() + blockOffsetSize + numberOfElements * extensibleArrayElementSize + 4;

                final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

                verifySignature(bb, EXTENSIBLE_ARRAY_DATA_BLOCK_SIGNATURE);

                // Version Number
                final byte version = bb.get();
                if (version != 0) {
                    throw new HdfException("Unsupported extensible array data block version detected. Version: " + version);
                }

                final int clientId = bb.get();
                if (clientId != ExtensibleArrayIndex.this.clientId) {
                    throw new HdfException("Extensible array client ID mismatch. Possible file corruption detected");
                }

                final long headerAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                if (headerAddress != ExtensibleArrayIndex.this.headerAddress) {
                    throw new HdfException("Extensible array data block header address missmatch");
                }

                long blockOffset = readBytesAsUnsignedLong(bb, blockOffsetSize);

                // Page bitmap

                // Data block addresses
                for (int i = 0; i < numberOfElements; i++) {
                    readElement(bb, hdfFc);
                }

                // Checksum
            }

        }

        private class ExtensibleArraySecondaryBlock {

            private ExtensibleArraySecondaryBlock(HdfFileChannel hdfFc, long address) {

                final int i1 = 6 + hdfFc.getSizeOfOffsets() +
                        blockOffsetSize +
                        // Page Bitmap ?
                        // TODO what about when secondary blocks also get expanded
                        minNumberOfDataBlockPointers * extensibleArrayElementSize +
                        4; // checksum


                final ByteBuffer bb = hdfFc.readBufferFromAddress(address, i1);

                verifySignature(bb, EXTENSIBLE_ARRAY_SECONDARY_BLOCK_SIGNATURE);

                // Version Number
                final byte version = bb.get();
                if (version != 0) {
                    throw new HdfException("Unsupported fixed array data block version detected. Version: " + version);
                }

                final int clientId = bb.get();
                if (clientId != ExtensibleArrayIndex.this.clientId) {
                    throw new HdfException("Extensible array client ID mismatch. Possible file corruption detected");
                }

                final long headerAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                if (headerAddress != ExtensibleArrayIndex.this.headerAddress) {
                    throw new HdfException("Extensible array data block header address missmatch");
                }

                final long blockOffset = readBytesAsUnsignedLong(bb, blockOffsetSize);

                // TODO page bitmap

                // Data block addresses
                for (int i = 0; i < minNumberOfDataBlockPointers; i++) {
                    long dataBlockAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                    new ExtensibleArrayDataBlock(hdfFc, dataBlockAddress);
                }

            }

        }

        private void readElement(ByteBuffer bb, HdfFileChannel hdfFc) {
            final long chunkAddress = readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
            if (chunkAddress != UNDEFINED_ADDRESS) {
                final int[] chunkOffset = Utils.linearIndexToDimensionIndex((elementCounter * unfilteredChunkSize) / elementSize, datasetDimensions);
                if (filtered) {
                    final int chunkSizeInBytes = Utils.readBytesAsUnsignedInt(bb, extensibleArrayElementSize - hdfFc.getSizeOfOffsets() - 4);
                    final BitSet filterMask = BitSet.valueOf(new byte[] { bb.get(), bb.get(), bb.get(), bb.get() });
                    chunks.add(new ChunkImpl(chunkAddress, chunkSizeInBytes, chunkOffset, filterMask));
                } else { // Not filtered
                    chunks.add(new ChunkImpl(chunkAddress, unfilteredChunkSize, chunkOffset));
                }
                elementCounter++;
            }
        }

    }

    private void verifySignature(ByteBuffer bb, byte[] expectedSignature) {
        byte[] actualSignature = new byte[expectedSignature.length];
        bb.get(actualSignature, 0, expectedSignature.length);

        // Verify signature
        if (!Arrays.equals(expectedSignature, actualSignature)) {
            String signatureStr = new String(expectedSignature);
            throw new HdfException("Signature '" + signatureStr + "' not matched, at address ");
        }
    }

    /**
     * This counts the number of elements (chunks) in a data block. The scheme used to assign blocks is described here
     * https://doi.org/10.1007/3-540-48447-7_4
     */
    /* package */ static class ExtensibleArrayCounter {

        private final int minNumberOfElementsInDataBlock;

        private int blockSizeMultiplier = 1;
        private int numberOfBlocks = 1;
        private int blockCounter = 0;
        private boolean increaseNumberOfBlocksNext = false;

        /* package */ ExtensibleArrayCounter(int minNumberOfElementsInDataBlock) {
            this.minNumberOfElementsInDataBlock = minNumberOfElementsInDataBlock;
        }

        public int getNextNumberOfChunks() {
            if (blockCounter < numberOfBlocks) {
                blockCounter++;
            } else if (increaseNumberOfBlocksNext) {
                increaseNumberOfBlocksNext = false;
                numberOfBlocks *= 2;
                blockCounter = 1;
            } else {
                increaseNumberOfBlocksNext = true;
                blockSizeMultiplier *= 2;
                blockCounter = 1;
            }
            return blockSizeMultiplier * minNumberOfElementsInDataBlock;
        }
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return chunks;
    }
}
