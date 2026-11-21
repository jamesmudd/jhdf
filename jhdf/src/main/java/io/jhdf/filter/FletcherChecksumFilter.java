/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.checksum.Fletcher32;
import io.jhdf.exceptions.HdfChecksumMismatchException;
import io.jhdf.exceptions.HdfFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.util.Arrays.copyOf;

/**
 * HDF5 'fletcher32' checksum filter (filter id 3).
 * <p>
 * On decode the 4 byte Fletcher32 checksum stored at the end of the chunk is
 * verified against the rest of the chunk data and then removed. If the checksum
 * does not match an {@link HdfChecksumMismatchException} is thrown, this likely
 * indicates file corruption.
 * <p>
 * This is a Java port of {@code H5Z__filter_fletcher32()} from
 * {@code H5Zfletcher32.c} in the HDF5 C library
 * <a href="https://github.com/HDFGroup/hdf5">https://github.com/HDFGroup/hdf5</a>.
 *
 * @author James Mudd
 */
public class FletcherChecksumFilter implements Filter {

	public static final int ID = 3;

	private static final Logger logger = LoggerFactory.getLogger(FletcherChecksumFilter.class);

	/** Number of bytes in the checksum */
	private static final int CHECKSUM_LENGTH = 4;

	@Override
	public int getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "fletcher32";
	}

	@Override
	public byte[] decode(byte[] encodedData, int[] filterData) {
		// The checksum is stored at the end of the chunk, so anything shorter than
		// the checksum itself cannot contain any data
		if (encodedData.length <= CHECKSUM_LENGTH) {
			throw new HdfFilterException("Fletcher32 checksum buffer too short. Expected at least "
				+ (CHECKSUM_LENGTH + 1) + " bytes but got " + encodedData.length);
		}

		final int dataLength = encodedData.length - CHECKSUM_LENGTH;
		final int calculatedChecksum = Fletcher32.checksum(encodedData, 0, dataLength);
		final int storedChecksum = ByteBuffer.wrap(encodedData, dataLength, CHECKSUM_LENGTH)
			.order(ByteOrder.LITTLE_ENDIAN)
			.getInt();

		// There was a bug in the Fletcher32 checksum calculation in the HDF5 library
		// before v1.6.3. The checksum value was not consistent between big-endian
		// and little-endian systems. This was fixed in Release 1.6.3. Check both the
		// correct checksum and the buggy one to remain compatible with files written
		// by Release 1.6.2 and earlier.
		final int reversedChecksum = reverseWordOrder(calculatedChecksum);

		if (storedChecksum != calculatedChecksum && storedChecksum != reversedChecksum) {
			logger.error("Fletcher32 checksum mismatch, possible file corruption. stored checksum = [{}] != calculated checksum = [{}]",
				storedChecksum, calculatedChecksum);
			throw new HdfChecksumMismatchException(storedChecksum, calculatedChecksum);
		}

		// Remove the checksum, the remaining data is returned
		return copyOf(encodedData, dataLength);
	}

	@Override
	public byte[] encode(byte[] data, int[] filterData) {
		final int checksum = Fletcher32.checksum(data);

		// Append the checksum to the raw data for storage
		final ByteBuffer buffer = ByteBuffer.allocate(data.length + CHECKSUM_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(data);
		buffer.putInt(checksum);

		return buffer.array();
	}

	/**
	 * Swaps the byte order of each 16-bit word of the checksum. Used to check for
	 * checksums written by buggy HDF5 libraries before v1.6.3.
	 *
	 * @param checksum the checksum to swap the words of
	 * @return the checksum with each 16-bit word byte-swapped
	 */
	private static int reverseWordOrder(int checksum) {
		final int lowWord = swapBytes(checksum & 0xffff);
		final int highWord = swapBytes((checksum >>> 16) & 0xffff);
		return (highWord << 16) | lowWord;
	}

	/**
	 * Swaps the two bytes of a 16-bit value.
	 *
	 * @param word the 16-bit value to swap the bytes of
	 * @return the 16-bit value with its bytes swapped
	 */
	private static int swapBytes(int word) {
		return ((word & 0xff) << 8) | ((word >>> 8) & 0xff);
	}
}
