/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.jhdf.TestUtils.flatten;
import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;

class BitfieldDatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "bitfield_datasets.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		hdfFile.close();
	}

	private static Stream<Arguments> testBitfieldDataset() {
		return Stream.of(
				Arguments.of(hdfFile.getDatasetByPath("/bitfield")),
				Arguments.of(hdfFile.getDatasetByPath("/chunked_bitfield")),
				Arguments.of(hdfFile.getDatasetByPath("/compressed_chunked_bitfield"))
		);
	}

	@ParameterizedTest
	@MethodSource
	void testBitfieldDataset(Dataset dataset) {
		assertThat(dataset.getDimensions(), is(equalTo(new int[]{15})));
		assertThat(dataset.getMaxSize(), is(equalTo(new int[]{15})));
		assertThat(dataset.isCompound(), is(false));
		assertThat(dataset.isEmpty(), is(false));
		assertThat(dataset.isScalar(), is(false));
		assertThat(dataset.getJavaType(), is(boolean.class));
		assertThat(dataset.getFillValue(), is(nullValue()));

		// Now check the data
		Object data = dataset.getData();
		assertThat(data, isA(boolean[].class));
		boolean[] boolData = (boolean[]) data;

		for (int i = 0; i < boolData.length; i++) {
			assertThat(boolData[i], is(i % 2 != 0));
		}
	}

	@Test
	void test2DBitfieldDataset() {
		Dataset dataset = hdfFile.getDatasetByPath("compressed_chunked_2d_bitfield");

		// Should be 2D
		assertThat(dataset.getDimensions(), is(equalTo(new int[]{3, 5})));
		assertThat(dataset.getMaxSize(), is(equalTo(new int[]{3, 5})));
		assertThat(dataset.isCompound(), is(false));
		assertThat(dataset.isEmpty(), is(false));
		assertThat(dataset.isScalar(), is(false));
		assertThat(dataset.getJavaType(), is(boolean.class));
		assertThat(dataset.getFillValue(), is(nullValue()));

		// Now check the data
		Object data = dataset.getData();
		assertThat(data, isA(boolean[][].class)); // A 2D array

		Object[] boolData = flatten(data);

		// And check the values
		for (int i = 0; i < boolData.length; i++) {
			assertThat(boolData[i], is(i % 2 != 0));
		}
	}

	@Test
	void testScalarBitfieldDataset() {
		Dataset dataset = hdfFile.getDatasetByPath("scalar_bitfield");

		assertThat(dataset.getDimensions(), is(equalTo(new int[]{})));
		assertThat(dataset.getMaxSize(), is(equalTo(new int[]{})));
		assertThat(dataset.isCompound(), is(false));
		assertThat(dataset.isEmpty(), is(false));
		assertThat(dataset.isScalar(), is(true));
		assertThat(dataset.getJavaType(), is(Boolean.class));
		assertThat(dataset.getFillValue(), is(nullValue()));

		Object data = dataset.getData();
		assertThat(data, isA(Boolean.class));

		// Check the value
		assertThat(data, is(true));
	}
}
