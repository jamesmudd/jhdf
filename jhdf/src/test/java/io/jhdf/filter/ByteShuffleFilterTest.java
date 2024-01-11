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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ByteShuffleFilterTest {

	@Test
	void testShuffleElementSize2() {
		byte[] input = new byte[]{1, 3, 5, 7, 9, 2, 4, 6, 8, 10};
		ByteShuffleFilter byteShuffleFilter = new ByteShuffleFilter();

		byte[] output = byteShuffleFilter.decode(input, new int[]{2});
		assertThat(output, is(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
	}

	// See https://github.com/jamesmudd/jhdf/issues/318
	// When input length is not a multiple of the element size
	@Test
	void testShuffleElementSize4() {
		byte[] input = new byte[]{1, 5, 2, 6, 3, 7, 4, 8, 9, 10};
		ByteShuffleFilter byteShuffleFilter = new ByteShuffleFilter();

		byte[] output = byteShuffleFilter.decode(input, new int[]{4});
		assertThat(output, is(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
	}

	@Test
	void testShuffleWithSingleValue() {
		byte[] input = new byte[]{1};
		ByteShuffleFilter byteShuffleFilter = new ByteShuffleFilter();

		byte[] output = byteShuffleFilter.decode(input, new int[]{2});
		assertThat(output, is(new byte[]{1}));
	}

	// See https://github.com/jamesmudd/jhdf/issues/318
	// When input length is not a multiple of the element size
	@Test
	void testShuffleWithMismatchedLength() {
		byte[] input = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, -40, -50, -60, -70};
		ByteShuffleFilter byteShuffleFilter = new ByteShuffleFilter();

		byte[] output = byteShuffleFilter.decode(input, new int[]{8});
		assertThat(output, is(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, -40, -50, -60, -70}));
	}


}
