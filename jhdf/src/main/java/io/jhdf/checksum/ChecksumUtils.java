/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.checksum;

import io.jhdf.exceptions.HdfChecksumMismatchException;

import java.nio.ByteBuffer;

public final class ChecksumUtils {

	private ChecksumUtils() {
		throw new AssertionError("No instances of ChecksumUtils");
	}

	/**
	 * Checks the last 4 bytes of the buffer ate the the Jenkins Lookup 3 Checksum of the rest of the buffer.
	 *
	 * @param buffer
	 * @throws HdfChecksumMismatchException if the checksum is incorrect.
	 */
	public static void validateChecksum(ByteBuffer buffer) {
		int bytesToRead = buffer.limit() - 4 - buffer.position();
		byte[] bytes = new byte[bytesToRead];
		buffer.get(bytes);
		int calculatedChecksum = checksum(bytes);
		int storedChecksum = buffer.getInt();
		if(calculatedChecksum != storedChecksum) {
			throw new HdfChecksumMismatchException(storedChecksum, calculatedChecksum);
		}

	}

	public static int checksum(ByteBuffer buffer) {
		return JenkinsLookup3HashLittle.hash(buffer);
	}

	public static int checksum(byte[] bytes) {
		return JenkinsLookup3HashLittle.hash(bytes);
	}

}
