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
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;

public class Lz4Filter implements Filter {

	private final LZ4FastDecompressor lzz4Decompressor = LZ4Factory.fastestJavaInstance().fastDecompressor();

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
	 * The name of this filter, "lz4"
	 *
	 * @return "lz4"
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
		byte[] compressedBlock = new byte[0];

		long blocks2;
		if(decompressedBlockSize > totalDecompressedSize) {
			blocks2 = 1;
		} else {
			blocks2 = totalDecompressedSize / decompressedBlockSize;
		}

		int offset = 0;
		for (long i = 0; i < blocks2; i++) {
			final int compressedBlockSize = byteBuffer.getInt();
			if(compressedBlockSize > compressedBlock.length) {
				compressedBlock = new byte[compressedBlockSize];
			}
			byteBuffer.get(compressedBlock, 0, compressedBlockSize);
			final int decompressedBytes;
			if (compressedBlockSize == decompressedBlockSize) {
				System.arraycopy(compressedBlock, 0, decompressed, offset, compressedBlockSize);
				decompressedBytes = compressedBlockSize;
			} else {
				decompressedBytes = lzz4Decompressor.decompress(compressedBlock, 0,
					decompressed, offset,
					decompressedBlockSize);
			}
			offset += decompressedBytes;
		}

		if(byteBuffer.hasRemaining()) {
			byteBuffer.get(decompressed, offset, Math.min(decompressed.length -offset, byteBuffer.remaining()));
		}

		return decompressed;
	}
}
