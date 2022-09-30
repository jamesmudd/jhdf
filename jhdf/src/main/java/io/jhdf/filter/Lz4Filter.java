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
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;

public class Lz4Filter implements Filter {

	/**
	 * Id defined in <a href="https://support.hdfgroup.org/services/filters.html">...</a>
	 *
	 * @return Defined value, 32004
	 */
	@Override
	public int getId() {
		return 32004;
	}

	/**
	 * The name of this filter, "lzf
	 *
	 * @return "lzf"
	 */
	@Override
	public String getName() {
		return "lz4";
	}

	@Override
	public byte[] decode(byte[] encodedData, int[] filterData) {
		// See https://support.hdfgroup.org/services/filters/HDF5_LZ4.pdf
		ByteBuffer byteBuffer = ByteBuffer.wrap(encodedData);

		final long totalDecompressedSize = Utils.readBytesAsUnsignedLong(byteBuffer, 8);
		final byte[] decompressed = new byte[Math.toIntExact(totalDecompressedSize)];

		final int decompressedBlockSize = Utils.readBytesAsUnsignedInt(byteBuffer, 4);
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
			offset += decompressedBytes;
		}

		if(byteBuffer.hasRemaining()) {
			byteBuffer.get(decompressed, offset, byteBuffer.remaining());
		}

		return decompressed;
	}
}
