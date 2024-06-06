/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.exceptions.UnsupportedHdfException;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.nio.ByteBuffer;

public class BitShuffleFilter implements Filter {

	// Constants see https://github.com/kiyo-masui/bitshuffle/blob/master/src/bitshuffle_internals.h#L32
	private static final int BSHUF_MIN_RECOMMEND_BLOCK = 128;
	private static final int BSHUF_BLOCKED_MULT = 8; // Block sizes must be multiple of this.
	private static final int BSHUF_TARGET_BLOCK_SIZE_B = 8192;

	public static final int NO_COMPRESSION = 0;
	// https://github.com/kiyo-masui/bitshuffle/blob/master/src/bshuf_h5filter.h#L46
	public static final int LZ4_COMPRESSION = 2;
	public static final int ZSTD_COMPRESSION = 3;

	private final LazyInitializer<LZ4SafeDecompressor> lzz4Decompressor = new LazyInitializer<LZ4SafeDecompressor>() {
		@Override
		protected LZ4SafeDecompressor initialize() {
			return LZ4Factory.fastestJavaInstance().safeDecompressor();
		}
	};

	@Override
	public int getId() {
		return 32008;
	}

	@Override
	public String getName() {
		return "bitshuffle";
	}

	@Override
	public byte[] decode(byte[] encodedData, int[] filterData) {
		final int blockSize = filterData[3] == 0 ? getDefaultBlockSize(filterData[2]) : filterData[3];
		final int blockSizeBytes = blockSize * filterData[2];

		switch (filterData[4]) {
			case NO_COMPRESSION:
				return noCompression(encodedData, filterData, blockSizeBytes);
			case LZ4_COMPRESSION:
				// See https://support.hdfgroup.org/services/filters/HDF5_LZ4.pdf
				return lz4Compression(encodedData, filterData);
			case ZSTD_COMPRESSION:
				throw new UnsupportedHdfException("Bitshuffle zstd not implemented");
			default:
				throw new HdfFilterException("Unknown compression type: " + filterData[4]);
		}
	}

	private byte[] lz4Compression(byte[] encodedData, int[] filterData) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(encodedData);

		final long totalDecompressedSize = Utils.readBytesAsUnsignedLong(byteBuffer, 8);
		final byte[] decompressed = new byte[Math.toIntExact(totalDecompressedSize)];

		final int decompressedBlockSize = Utils.readBytesAsUnsignedInt(byteBuffer, 4);
		final byte[] decomressedBuffer = new byte[decompressedBlockSize];
		byte[] compressedBuffer = new byte[0];

		long blocks2;
		if (decompressedBlockSize > totalDecompressedSize) {
			blocks2 = 1;
		} else {
			blocks2 = totalDecompressedSize / decompressedBlockSize;
		}

		int offset = 0;
		for (long i = 0; i < blocks2; i++) {
			final int compressedBlockLength = byteBuffer.getInt();
			if(compressedBlockLength > compressedBuffer.length) {
				compressedBuffer = new byte[compressedBlockLength];
			}
			byteBuffer.get(compressedBuffer, 0, compressedBlockLength);

			try {
				final int decompressedBytes = lzz4Decompressor.get().decompress(compressedBuffer, 0, compressedBlockLength, decomressedBuffer, 0);
				unshuffle(decomressedBuffer, decompressedBytes, decompressed, offset, filterData[2]);
				offset += decompressedBytes;
			} catch (Exception e) {
				throw new HdfFilterException("Failed LZ4 decompression", e);
			}

		}

		if (byteBuffer.hasRemaining()) {
			byteBuffer.get(decompressed, offset, byteBuffer.remaining());
		}

		return decompressed;
	}

	private byte[] noCompression(byte[] encodedData, int[] filterData, int blockSizeBytes) {
		final int blocks = encodedData.length / blockSizeBytes;
		final byte[] unshuffled = new byte[encodedData.length];
		for (int i = 0; i < blocks; i++) {
			byte[] blockData = new byte[blockSizeBytes];
			System.arraycopy(encodedData, i * blockSizeBytes, blockData, 0, blockSizeBytes);
			byte[] unshuffledBlock = new byte[blockSizeBytes];
			unshuffle(blockData, filterData[2], unshuffledBlock);
			System.arraycopy(unshuffledBlock, 0, unshuffled, i * blockSizeBytes, blockSizeBytes);
		}
		if (blocks * blockSizeBytes < encodedData.length) {
			int finalBlockSize = encodedData.length - blocks * blockSizeBytes;
			byte[] blockData = new byte[finalBlockSize];
			System.arraycopy(encodedData, blocks * blockSizeBytes, blockData, 0, finalBlockSize);
			byte[] unshuffledBlock = new byte[finalBlockSize];
			unshuffle(blockData, filterData[2], unshuffledBlock);
			System.arraycopy(unshuffledBlock, 0, unshuffled, blocks * blockSizeBytes, finalBlockSize);
		}
		return unshuffled;
	}

	protected void unshuffle(byte[] shuffledBuffer, int elementSize, byte[] unshuffledBuffer) {
		unshuffle(shuffledBuffer, shuffledBuffer.length, unshuffledBuffer, 0, elementSize);
	}

	protected void unshuffle(byte[] shuffledBuffer, int shuffledLength, byte[] unshuffledBuffer, int unshuffledOffset, int elementSize) {
		final int elements = shuffledLength / elementSize;
		final int elementSizeBits = elementSize * 8;
		final int unshuffledOffsetBits = unshuffledOffset * 8;

		if (elements < 8) {
			// https://github.com/xerial/snappy-java/issues/296#issuecomment-964469607
			System.arraycopy(shuffledBuffer, 0, unshuffledBuffer, 0, shuffledLength);
			return;
		}

		final int elementsToShuffle = elements - elements % 8;
		final int elementsToCopy = elements - elementsToShuffle;

		int pos = 0;
		for (int i = 0; i < elementSizeBits; i++) {
			for (int j = 0; j < elementsToShuffle; j++) {
				boolean bit = Utils.getBit(shuffledBuffer, pos);
				if (bit) {
					Utils.setBit(unshuffledBuffer, unshuffledOffsetBits + j * elementSizeBits + i, true);
				}
				pos++; // step through the input array
			}
		}

		System.arraycopy(shuffledBuffer, elementsToShuffle * elementSize, unshuffledBuffer, elementsToShuffle * elementSize, elementsToCopy * elementSize);
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
