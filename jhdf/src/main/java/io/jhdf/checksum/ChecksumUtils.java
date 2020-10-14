/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.checksum;

import io.jhdf.exceptions.HdfChecksumMismatch;

import java.nio.ByteBuffer;

public final class ChecksumUtils {

	private ChecksumUtils() {
		throw new AssertionError("No instances of ChecksumUtils");
	}

	public static void validateChecksum(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.limit() - 4];
		buffer.get(bytes);
		int calculatedChecksum = checksum(bytes);
		int storedChecksum = buffer.getInt();
		if(calculatedChecksum != storedChecksum) {
			throw new HdfChecksumMismatch(storedChecksum, calculatedChecksum);
		}

	}

	public static int checksum(ByteBuffer buffer) {
		return JenkinsLookup3HashLittle.hash(buffer);
	}

	public static int checksum(byte[] bytes) {
		return JenkinsLookup3HashLittle.hash(bytes);
	}

}
