package io.jhdf.filter;

import io.airlift.compress.lz4.Lz4Decompressor;
import io.airlift.compress.zstd.ZstdDecompressor;
import net.jpountz.lz4.LZ4DecompressorWithLength;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

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
				long tootalDecompressedSize = byteBuffer.getLong();
				int decompressedBlockSize = byteBuffer.getInt();
//				byte[] decompressed = new byte[Math.toIntExact(tootalDecompressedSize)];
				ByteBuffer decompressed = ByteBuffer.allocate(Math.toIntExact(tootalDecompressedSize));
				byte[] decomressedBuffer = new byte[decompressedBlockSize];
				long blocks = (tootalDecompressedSize / decompressedBlockSize);

				LZ4FastDecompressor lzz4Decompressor = LZ4Factory.safeInstance().fastDecompressor();

				for (long i = 0; i < blocks; i++) {
					int compressedBlockLength = byteBuffer.getInt();
					byte[] input = new byte[compressedBlockLength];
					byteBuffer.get(input);
					lzz4Decompressor.decompress(input, decomressedBuffer);
					decompressed.put(decomressedBuffer);
				}
				break;
			case 3: // Zstd
				ZstdDecompressor zstdDecompressor = new ZstdDecompressor();
//				zstdDecompressor.decompress(ByteBuffer.wrap(encodedData), decompressed);
				break;
		}

		BitSet shuffled = BitSet.valueOf(encodedData);

		return encodedData;
	}
}
