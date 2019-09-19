package io.jhdf.dataset.chunked.indexing;

import io.jhdf.Constants;
import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ExtensibleArrayIndex implements ChunkIndex {

    private static final byte[] EXTENSIBLE_ARRAY_HEADER_SIGNATURE = "EAHD".getBytes();
    private static final byte[] EXTENSIBLE_ARRAY_INDEX_BLOCK_SIGNATURE = "EAIB".getBytes();

    private final long headerAddress;
    private final int clientId;
    private final int elementSize;
    private final int numberOfElementsInIndexBlock;
    private final int numberOfElements;
    private final int numberOfSecondaryBlocks;

    private final List<Chunk> chunks = new ArrayList<>();
    private final int unfilteredChunkSize;
    private final int[] datasetDimensions;

    public ExtensibleArrayIndex(HdfFileChannel hdfFc, long address, int chunkSizeInBytes, int elementSize, int[] dimensions) {
        this.headerAddress = address;
        this.unfilteredChunkSize = chunkSizeInBytes;
        this.elementSize = elementSize;
        this.datasetDimensions = dimensions;

        final int headerSize = 16 + hdfFc.getSizeOfOffsets() + 6*hdfFc.getSizeOfLengths();
        final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

        byte[] formatSignatureBytes = new byte[4];
        bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);

        // Verify signature
        if (!Arrays.equals(EXTENSIBLE_ARRAY_HEADER_SIGNATURE, formatSignatureBytes)) {
            throw new HdfException("Extensible array header signature 'EAHD' not matched, at address " + address);
        }

        // Version Number
        final byte version = bb.get();
        if (version != 0) {
            throw new HdfException("Unsupported extensible array index version detected. Version: " + version);
        }

        clientId = bb.get();
        elementSize = bb.get();

        final int maxNumberOfElementsBits = bb.get();
        numberOfElementsInIndexBlock = bb.get();
        final int minNumberOfElementsInDataBlock = bb.get();
        final int minNumberOfDataBlockPointers = bb.get();
        final int maxNumberOfElementsInDataBlockPageBits = bb.get();

        numberOfSecondaryBlocks = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        final int secondaryBlockSize = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        final int numberOfDataBlocks = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());
        final int dataBlockSize = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        final int maxIndexSet = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        numberOfElements = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        final int indexBlockAddress = Utils.readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

        new ExtensibleArrayIndexBlock(hdfFc, indexBlockAddress);

        // Checksum
    }

    private class ExtensibleArrayIndexBlock {

        public ExtensibleArrayIndexBlock(HdfFileChannel hdfFc, long address) {

            // Figure out the size of the index block
            final int headerSize = 6 + hdfFc.getSizeOfOffsets()
                    + elementSize * numberOfElementsInIndexBlock // Elements
                    + numberOfElementsInIndexBlock * hdfFc.getSizeOfOffsets() // Addresses of the elements above???
                    + numberOfSecondaryBlocks * hdfFc.getSizeOfOffsets() // Secondary block addresses.
                    + 4; // checksum


            final ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

            byte[] formatSignatureBytes = new byte[4];
            bb.get(formatSignatureBytes, 0, formatSignatureBytes.length);

            // Verify signature
            if (!Arrays.equals(EXTENSIBLE_ARRAY_INDEX_BLOCK_SIGNATURE, formatSignatureBytes)) {
                throw new HdfException("Extensible array index block signature 'EAIB' not matched, at address " + address);
            }

            // Version Number
            final byte version = bb.get();
            if (version != 0) {
                throw new HdfException("Unsupported fixed array data block version detected. Version: " + version);
            }

            final int clientId = bb.get();
            if (clientId != ExtensibleArrayIndex.this.clientId) {
                throw new HdfException("Extensible array client ID mismatch. Possible file corruption detected");
            }

            final long headerAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
            if (headerAddress != ExtensibleArrayIndex.this.headerAddress) {
                throw new HdfException("Extensible array data block header address missmatch");
            }

            if (clientId == 0) { // Not filtered
            for (int i = 0; i < numberOfElementsInIndexBlock; i++) {
                final long chunkAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
                if (chunkAddress != Constants.UNDEFINED_ADDRESS) {
                    final int[] chunkOffset = Utils.linearIndexToDimensionIndex((i * unfilteredChunkSize) / elementSize, datasetDimensions);
                    chunks.add(new ChunkImpl(chunkAddress, unfilteredChunkSize, chunkOffset));
                }
            }
            } else  if (clientId == 1) { // Filtered

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
