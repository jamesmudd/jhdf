/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.checksum.JenkinsLookup3HashLittle;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;

class JenkinsLookup3HashLittleTest {

	// Examples taken from http://burtleburtle.net/bob/c/lookup3.c driver5
	static Stream<Arguments> testCases() {
		return Stream.of(
			//1D
			Arguments.of("".getBytes(US_ASCII), 0, 0xdeadbeef),
			Arguments.of("".getBytes(US_ASCII), 0xdeadbeef, 0xbd5b7dde),
			Arguments.of("".getBytes(US_ASCII), 0xbd5b7dde, 0x9c093ccd),
			Arguments.of("Four score and seven years ago".getBytes(US_ASCII), 0, 0x17770551),
			Arguments.of("Four score and seven years ago".getBytes(US_ASCII), 1, 0xcd628161)
		);
	}

	@ParameterizedTest
	@MethodSource("testCases")
	void testJenkinsHashLittle(byte[] toHash, int initialValue, int expectedHash) {
		final int hash = JenkinsLookup3HashLittle.hash(toHash, initialValue);
		assertThat(hash, Matchers.is(expectedHash));
	}

	@ParameterizedTest
	@MethodSource("testCases")
	void testJenkinsHashLittleBuffer(byte[] toHash, int initialValue, int expectedHash) {
		final int hash = JenkinsLookup3HashLittle.hash(ByteBuffer.wrap(toHash), initialValue);
		assertThat(hash, Matchers.is(expectedHash));
	}

}
