package io.jhdf.filter;

import io.airlift.compress.zstd.ZstdDecompressor;
import io.jhdf.Utils;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

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
//		ByteBuffer decompressed = ByteBuffer.allocate(encodedData.length * 50);
		switch (filterData[4]) {
			case 0: // No compresssion
				break;
			// https://github.com/kiyo-masui/bitshuffle/blob/master/src/bshuf_h5filter.h#L46
			case 2: // LZ4
				// See https://support.hdfgroup.org/services/filters/HDF5_LZ4.pdf
				ByteBuffer byteBuffer = ByteBuffer.wrap(encodedData);
				long totalDecompressedSize = byteBuffer.getLong();
				int decompressedBlockSize = byteBuffer.getInt();
				byte[] decompressed = new byte[Math.toIntExact(totalDecompressedSize)];
				byte[] decomressedBuffer = new byte[decompressedBlockSize];
				long blocks = (totalDecompressedSize / decompressedBlockSize);

				LZ4FastDecompressor lzz4Decompressor = LZ4Factory.safeInstance().fastDecompressor();

				int offset = 0;
				for (long i = 0; i < blocks; i++) {
					int compressedBlockLength = byteBuffer.getInt();
					byte[] input = new byte[compressedBlockLength];
					byteBuffer.get(input);
					lzz4Decompressor.decompress(input, decomressedBuffer);
					unshuffle(decomressedBuffer, elementSizeBits, decompressed, offset);
					offset += decompressedBlockSize;
//					decompressed.put(decomressedBuffer);

				}
//				decompressed.flip();
//
//				BitSet shuffled = BitSet.valueOf(decompressed);
//				BitSet unshuffled = new BitSet(Math.toIntExact(totalDecompressedSize * 8));
//
//				int elements = Math.toIntExact(totalDecompressedSize / filterData[2]);
//				int pos = 0;
//				for (int i = 0; i < elementSizeBits; i++) {
//					for (int j = 0; j < elements; j++) {
//						unshuffled.set(j * elementSizeBits + i,  shuffled.get(pos));
//						pos++; // step through the input array
//					}
//				}
//
//				return unshuffled.toByteArray();
				return decompressed;
			case 3: // Zstd
				ZstdDecompressor zstdDecompressor = new ZstdDecompressor();
//				zstdDecompressor.decompress(ByteBuffer.wrap(encodedData), decompressed);
				break;
		}



		return encodedData;
	}

	private void unshuffle(byte[] decomressedBuffer, int elementSizeBits, byte[] decompressed, int offset) {
		int decompressedByfferBits = decomressedBuffer.length * 8;
		int elements = decompressedByfferBits / elementSizeBits;
		for (int i = 0; i < elements; i++) {
			for (int j = 0; j < elementSizeBits; j++) {
				boolean bit = Utils.getBit(decomressedBuffer, i);
				if (bit) {
					Utils.setBit(decompressed, offset*8 + i, true);
				}

			}
		}
		for (int i = 0; i < decomressedBuffer.length * 8; i++) {
					}
	}
}
