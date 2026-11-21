/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.checksum;

import java.nio.ByteBuffer;

import static java.lang.Byte.toUnsignedInt;

/**
 * The Fletcher32 checksum as used by the HDF5 'fletcher32' filter.
 * <p>
 * This is a Java port of {@code H5_checksum_fletcher32()} from
 * {@code H5checksum.c} in the HDF5 C library
 * <a href="https://github.com/HDFGroup/hdf5">https://github.com/HDFGroup/hdf5</a>.
 * <p>
 * Note this is the HDF5 variant of Fletcher32, which differs slightly from the
 * algorithm described on the
 * <a href="https://en.wikipedia.org/wiki/Fletcher%27s_checksum">Wikipedia page</a>
 * for Fletcher's checksum. It processes the input as big-endian 16-bit words,
 * initialises the sums to zero, and handles an odd trailing byte by
 * left-aligning it into a 16-bit word.
 *
 * @author James Mudd
 */
public final class Fletcher32 {

	/**
	 * The largest number of 16-bit word sums that can be accumulated before
	 * reduction without numeric overflow (see the HDF5 C source).
	 */
	private static final int MAX_SUMS_BEFORE_REDUCTION = 360;

	private Fletcher32() {
		throw new AssertionError("No instances of Fletcher32");
	}

	/**
	 * Calculates the Fletcher32 checksum of the given bytes.
	 *
	 * @param bytes the bytes to checksum
	 * @return the Fletcher32 checksum
	 */
	public static int checksum(byte[] bytes) {
		return checksum(bytes, 0, bytes.length);
	}

	/**
	 * Calculates the Fletcher32 checksum of a sub-array of the given bytes.
	 *
	 * @param bytes  the bytes to checksum
	 * @param offset the offset of the sub-array to checksum
	 * @param length the length of the sub-array to checksum
	 * @return the Fletcher32 checksum
	 */
	public static int checksum(byte[] bytes, int offset, int length) {
		return checksum(ByteBuffer.wrap(bytes, offset, length));
	}

	/**
	 * Calculates the Fletcher32 checksum of the remaining bytes of the buffer. The
	 * buffer position is not modified.
	 *
	 * @param buffer the buffer to checksum
	 * @return the Fletcher32 checksum
	 */
	public static int checksum(ByteBuffer buffer) {
		final ByteBuffer data = buffer.duplicate();

		long sum1 = 0;
		long sum2 = 0;

		// Number of 16-bit words in the data
		int words = data.remaining() / 2;

		// Compute the checksum for pairs of bytes
		while (words > 0) {
			int wordsToSum = Math.min(words, MAX_SUMS_BEFORE_REDUCTION);
			words -= wordsToSum;
			while (wordsToSum-- > 0) {
				// Each pair of bytes is treated as a big-endian 16-bit word
				int word = (toUnsignedInt(data.get()) << 8) | toUnsignedInt(data.get());
				sum1 += word;
				sum2 += sum1;
			}
			// Reduce the sums to 16 bits to avoid numeric overflow
			sum1 = (sum1 & 0xffff) + (sum1 >> 16);
			sum2 = (sum2 & 0xffff) + (sum2 >> 16);
		}

		// Check for an odd number of bytes
		if (data.remaining() > 0) {
			// The trailing byte is left-aligned into a 16-bit word
			sum1 += toUnsignedInt(data.get()) << 8;
			sum2 += sum1;
			sum1 = (sum1 & 0xffff) + (sum1 >> 16);
			sum2 = (sum2 & 0xffff) + (sum2 >> 16);
		}

		// Second reduction step to reduce the sums to 16 bits
		sum1 = (sum1 & 0xffff) + (sum1 >> 16);
		sum2 = (sum2 & 0xffff) + (sum2 >> 16);

		return (int) ((sum2 << 16) | sum1);
	}
}
