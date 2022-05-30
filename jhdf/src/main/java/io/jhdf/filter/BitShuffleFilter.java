/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.exceptions.UnsupportedHdfException;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;

public class BitShuffleFilter implements Filter {

	// Constants see https://github.com/kiyo-masui/bitshuffle/blob/master/src/bitshuffle_internals.h#L32
	private static final int BSHUF_MIN_RECOMMEND_BLOCK = 128;
	private static final int BSHUF_BLOCKED_MULT = 8; // Block sizes must be multiple of this.
	private static final int BSHUF_TARGET_BLOCK_SIZE_B = 8192;

	public static final int NO_COMPRESSION = 0;
	public static final int LZ4_COMPRESSION = 2;
	public static final int ZSTD_COMPRESSION = 3;


	@Override public int getId() {
		return 32008;
	}

	@Override public String getName() {
		return "bitshuffle";
	}

	@Override public byte[] decode(byte[] encodedData, int[] filterData) {
		final int elementSizeBits = filterData[2] * 8;
		final int blockSize = filterData[3] == 0 ? getDefaultBlockSize(filterData[2]) : filterData[3];
		final int blockSizeBytes = blockSize * filterData[2];

		switch (filterData[4]) {
			case NO_COMPRESSION:
				final int blocks = encodedData.length / blockSizeBytes;
				final byte[] unshuffled = new byte[encodedData.length];
				for (int i = 0; i < blocks; i++) {
					byte[] blockData = new byte[blockSizeBytes];
					System.arraycopy(encodedData, i*blockSizeBytes, blockData, 0, blockSizeBytes);
					byte[] unshuffledBlock = new byte[blockSizeBytes];
					unshuffle(blockData, elementSizeBits, unshuffledBlock);
					System.arraycopy(unshuffledBlock, 0, unshuffled, i*blockSizeBytes, blockSizeBytes);
				}
				if(blocks*blockSizeBytes < encodedData.length) {
					int finalBlockSize = encodedData.length - blocks*blockSizeBytes;
					byte[] blockData = new byte[finalBlockSize];
					System.arraycopy(encodedData, blocks*blockSizeBytes, blockData, 0, finalBlockSize);
					byte[] unshuffledBlock = new byte[finalBlockSize];
					unshuffle(blockData, elementSizeBits, unshuffledBlock);
					System.arraycopy(unshuffledBlock, 0, unshuffled, blocks*blockSizeBytes, finalBlockSize);
				}
				return unshuffled;
			// https://github.com/kiyo-masui/bitshuffle/blob/master/src/bshuf_h5filter.h#L46
			case LZ4_COMPRESSION:
				// See https://support.hdfgroup.org/services/filters/HDF5_LZ4.pdf
				ByteBuffer byteBuffer = ByteBuffer.wrap(encodedData);
				final long totalDecompressedSize = Utils.readBytesAsUnsignedLong(byteBuffer, 8);
				final int decompressedBlockSize = Utils.readBytesAsUnsignedInt(byteBuffer, 4);
				final byte[] decompressed = new byte[Math.toIntExact(totalDecompressedSize)];
				final byte[] decomressedBuffer = new byte[decompressedBlockSize];
				final byte[] compressedBuffer = new byte[decompressedBlockSize * 2]; // Assume compression never inflates by more than 2x
				long blocks2;
				if(decompressedBlockSize > totalDecompressedSize) {
					blocks2 = 1;
				} else {
					blocks2 = totalDecompressedSize / decompressedBlockSize;
				}

				final LZ4SafeDecompressor lzz4Decompressor = LZ4Factory.safeInstance().safeDecompressor();

				int offset = 0;
				for (long i = 0; i < blocks2; i++) {
					final int compressedBlockLength = byteBuffer.getInt();
					byteBuffer.get(compressedBuffer, 0, compressedBlockLength);
					final int decompressedBytes = lzz4Decompressor.decompress(compressedBuffer, 0, compressedBlockLength, decomressedBuffer, 0);
					byte[] decompressedData = new byte[decompressedBytes];
					byte[] unshuffledBlock = new byte[decompressedBytes];
					System.arraycopy(decomressedBuffer, 0,decompressedData,0,decompressedBytes);
					unshuffle(decompressedData, elementSizeBits, unshuffledBlock);
					System.arraycopy(unshuffledBlock, 0, decompressed, offset, decompressedBytes);
					offset += decompressedBytes;
				}

				if(byteBuffer.hasRemaining()) {
					byteBuffer.get(decompressed, offset, byteBuffer.remaining());
				}

				return decompressed;
			case ZSTD_COMPRESSION:
				throw new UnsupportedHdfException("Bitshuffle zstd not implemented");
			default:
				throw new HdfFilterException("Unknown compression type: " + filterData[4]);
		}
	}

	protected void unshuffle(byte[] decomressedBuffer, int elementSizeBits, byte[] decompressed) {
		final int decompressedByfferBits = decomressedBuffer.length * 8;
		final int elements = decompressedByfferBits / elementSizeBits;
		final int elementSizeBytes = elementSizeBits /  8;

		if(elements<8) {
			// https://github.com/xerial/snappy-java/issues/296#issuecomment-964469607
			System.arraycopy(decomressedBuffer, 0, decompressed, 0, decomressedBuffer.length);
			return;
		}

		int elementsToShuffle = elements - elements % 8;
		int elementsToCopy = elements - elementsToShuffle;

		int pos = 0;
		for (int i = 0; i < elementSizeBits; i++) {
			for (int j = 0; j < elementsToShuffle; j++) {
				boolean bit = Utils.getBit(decomressedBuffer, pos);
				if (bit) {
					Utils.setBit(decompressed, j*elementSizeBits + i, true);
				}
				pos++; // step through the input array
			}
		}

		System.arraycopy(decomressedBuffer, elementsToShuffle * elementSizeBytes, decompressed, elementsToShuffle * elementSizeBytes, elementsToCopy * elementSizeBytes);

	}

	// https://github.com/kiyo-masui/bitshuffle/blob/master/src/bitshuffle_core.c#L1830
	// See method bshuf_default_block_size
	private int getDefaultBlockSize(int elementSize) {
		int defaultBlockSize = BSHUF_TARGET_BLOCK_SIZE_B / elementSize;
		// Ensure it is a required multiple.
		defaultBlockSize = (defaultBlockSize / BSHUF_BLOCKED_MULT) * BSHUF_BLOCKED_MULT;
		return Integer.max(defaultBlockSize, BSHUF_MIN_RECOMMEND_BLOCK);
	}
}
