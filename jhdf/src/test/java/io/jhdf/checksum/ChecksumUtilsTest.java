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
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChecksumUtilsTest {

	private static final byte[] TEST_BYTES = "".getBytes(US_ASCII);
	private static final int EXPECTED_CHECKSUM = 0xdeadbeef;
	private static final int INCORRECT_CHECKSUM = 0xabcdef;

	@Test
	void testChecksumsCalculateCorrectly() {
		assertThat(ChecksumUtils.checksum(TEST_BYTES), is(EXPECTED_CHECKSUM));
		assertThat(ChecksumUtils.checksum(ByteBuffer.wrap(TEST_BYTES)), is(EXPECTED_CHECKSUM));
	}

	@Test
	void testChecksumAtEndOfBufferValidatesSuccessfully() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(TEST_BYTES.length + 4);
		byteBuffer.put(TEST_BYTES);
		byteBuffer.putInt(EXPECTED_CHECKSUM);
		byteBuffer.rewind();
		ChecksumUtils.validateChecksum(byteBuffer);
	}

	@Test
	void testValidationFailsWhenChecksumIsInvalid() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(TEST_BYTES.length + 4);
		byteBuffer.put(TEST_BYTES);
		byteBuffer.putInt(INCORRECT_CHECKSUM);
		byteBuffer.rewind();
		assertThrows(HdfChecksumMismatchException.class, () -> ChecksumUtils.validateChecksum(byteBuffer));
	}
}
