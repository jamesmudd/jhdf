/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class ReadFiltersTest {

	private ByteArrayOutputStream outputStream;

	@BeforeEach
	void setUp() {
		outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));
	}


	static Stream<Arguments> getTests() {
		return Stream.of(
			Arguments.of("/hdf5/test_byteshuffle_compressed_datasets_latest.hdf5", "/float/float32", "shuffle (id=2) data=[4] -> deflate (id=1) data=[4]"),
			Arguments.of("/hdf5/bitshuffle_datasets.hdf5", "float32_bs8_comp2", "bitshuffle (id=32008) data=[0, 4, 4, 8, 2]")
		);
	}

	@ParameterizedTest
	@MethodSource("getTests")
	void testReadFilters(String resourcePath, String datasetPath, String expectedOutput) throws Exception {

		URL resource = ReadFiltersTest.class.getResource(resourcePath);
		Path absolutePath = Paths.get(resource.toURI()).toAbsolutePath();

		ReadFilters.main(new String[]{absolutePath.toString(), datasetPath});

		assertThat(outputStream.toString(), containsString(expectedOutput));
	}
}
