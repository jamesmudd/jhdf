/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Deflater;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeflatePipelineFilterTest {

	@Test
	void decodeWorks() {
		byte[] input = StringUtils.repeat( "TestString", 50).getBytes(StandardCharsets.UTF_8);
		byte[] compressedBuffer = new byte[1024];

		Deflater deflater = new Deflater();
		deflater.setInput(input);
		deflater.finish(); // This is all the input

		// Do the compression
		int size = deflater.deflate(compressedBuffer, 0, input.length);
		byte[] compressed = Arrays.copyOf(compressedBuffer, size);

		DeflatePipelineFilter deflatePipelineFilter = new DeflatePipelineFilter();
		byte[] decoded = deflatePipelineFilter.decode(compressed, new int[0]);

		assertThat(decoded, is(input));
	}

	@Test
	void decodeZeroLengthThrows() {
		DeflatePipelineFilter deflatePipelineFilter = new DeflatePipelineFilter();
		assertThrows(HdfFilterException.class, () ->  deflatePipelineFilter.decode(new byte[0], new int[0]));
	}

	@Test
	void decodeMalformedThrows()  {
		DeflatePipelineFilter deflatePipelineFilter = new DeflatePipelineFilter();
		assertThrows(HdfFilterException.class, () ->  deflatePipelineFilter.decode(new byte[]{1,2,3}, new int[0]));
	}
}
