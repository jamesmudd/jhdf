/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfFilterException;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.nio.ByteBuffer;

public class Lz4Filter implements Filter {

	private final LazyInitializer<LZ4FastDecompressor> lzz4Decompressor = new LazyInitializer<LZ4FastDecompressor>() {
		@Override
		protected LZ4FastDecompressor initialize() {
			return LZ4Factory.fastestJavaInstance().fastDecompressor();
		}
	};

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

		final long blocks = (totalDecompressedSize + decompressedBlockSize - 1) / decompressedBlockSize;

		int offset = 0;
		byte[] compressedBlock = new byte[0];

		for (long i = 0; i < blocks; i++) {
			final int compressedBlockSize = byteBuffer.getInt();
			if (compressedBlockSize > compressedBlock.length) {
				compressedBlock = new byte[compressedBlockSize];
			}
			byteBuffer.get(compressedBlock, 0, compressedBlockSize);

			final int blockSize = Math.min(decompressed.length - offset, decompressedBlockSize);

			if (compressedBlockSize == blockSize) {
				System.arraycopy(compressedBlock, 0, decompressed, offset, blockSize);
			} else {
				try {
					lzz4Decompressor.get().decompress(compressedBlock, 0,
						decompressed, offset,
						blockSize);
				} catch (Exception e) {
					throw new HdfFilterException("Failed LZ4 decompression", e);
				}
			}
			offset += blockSize;
		}

		return decompressed;
	}
}
