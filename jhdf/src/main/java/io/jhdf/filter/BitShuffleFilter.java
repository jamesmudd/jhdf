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
import io.jhdf.exceptions.UnsupportedHdfException;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;

public class BitShuffleFilter implements Filter {
	@Override public int getId() {
		return 32008;
	}

	@Override public String getName() {
		return "bitshuffle";
	}

	@Override public byte[] decode(byte[] encodedData, int[] filterData) {
		int elementSizeBits = filterData[2] * 8;
		int blockSize =filterData[3];
		switch (filterData[4]) {
			case 0: // No compresssion
				byte[] unshuffled = new byte[encodedData.length];
				unshuffle(encodedData, elementSizeBits, unshuffled);
				return unshuffled;
			// https://github.com/kiyo-masui/bitshuffle/blob/master/src/bshuf_h5filter.h#L46
			case 2: // LZ4
				// See https://support.hdfgroup.org/services/filters/HDF5_LZ4.pdf
				ByteBuffer byteBuffer = ByteBuffer.wrap(encodedData);
				long totalDecompressedSize = byteBuffer.getLong();
				int decompressedBlockSize = byteBuffer.getInt();
				byte[] decompressed = new byte[Math.toIntExact(totalDecompressedSize)];
				byte[] decomressedBuffer = new byte[decompressedBlockSize];
				long blocks = (totalDecompressedSize + decompressedBlockSize - 1) / decompressedBlockSize;


				LZ4SafeDecompressor lzz4Decompressor = LZ4Factory.safeInstance().safeDecompressor();
//				LZ4Factory.safeInstance().safeDecompressor()

				int offset = 0;
				for (long i = 0; i < blocks; i++) {
					int compressedBlockLength = byteBuffer.getInt();
					byte[] input = new byte[compressedBlockLength];
					byteBuffer.get(input);
					int decompressedBytes = lzz4Decompressor.decompress(input, decomressedBuffer);
					byte[] decompressedData = new byte[decompressedBytes];
					System.arraycopy(decomressedBuffer, 0,decompressedData,0,decompressedBytes);
					unshuffle(decompressedData, elementSizeBits, decompressed);
					offset += decompressedBlockSize;
				}

				return decompressed;
			case 3: // Zstd
				throw new UnsupportedHdfException("Bitshuffle zstd not implemented");
		}



		return encodedData;
	}

	protected void unshuffle(byte[] decomressedBuffer, int elementSizeBits, byte[] decompressed) {
		int decompressedByfferBits = decomressedBuffer.length * 8;
		int elements = decompressedByfferBits / elementSizeBits;

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

		System.arraycopy(decomressedBuffer, elementsToShuffle, decompressed, elementsToShuffle, elementsToCopy);

	}
}
