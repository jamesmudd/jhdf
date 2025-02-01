/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.Utils;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class VariableLengthDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_vlen_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_vlen_datasets_latest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() throws Exception {
		earliestHdfFile = loadTestHdfFile(HDF5_TEST_EARLIEST_FILE_NAME);
		latestHdfFile = loadTestHdfFile(HDF5_TEST_LATEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		earliestHdfFile.close();
		latestHdfFile.close();
	}

	@TestFactory
	Collection<DynamicNode> compressedChunkedDatasetReadTests() {
		// List of all the datasetPaths
		return Arrays.asList(
			dynamicContainer(HDF5_TEST_EARLIEST_FILE_NAME, Arrays.asList(
				dynamicTest("float32", createTest(earliestHdfFile, "vlen_float32_data")),
				dynamicTest("float32 - chunked", createTest(earliestHdfFile, "vlen_float32_data_chunked")),
				dynamicTest("float64", createTest(earliestHdfFile, "vlen_float64_data")),
				dynamicTest("float64 - chunked", createTest(earliestHdfFile, "vlen_float64_data_chunked")),
				dynamicTest("uint8", createTest(earliestHdfFile, "vlen_uint8_data")),
				dynamicTest("uint8 - chunked", createTest(earliestHdfFile, "vlen_uint8_data_chunked")),
				dynamicTest("uint16", createTest(earliestHdfFile, "vlen_uint16_data")),
				dynamicTest("uint16 - chunked", createTest(earliestHdfFile, "vlen_uint16_data_chunked")),
				dynamicTest("uint32", createTest(earliestHdfFile, "vlen_uint32_data")),
				dynamicTest("uint32 - chunked", createTest(earliestHdfFile, "vlen_uint32_data_chunked")),
				dynamicTest("uint64", createTest(earliestHdfFile, "vlen_uint64_data")),
				dynamicTest("uint64 - chunked", createTest(earliestHdfFile, "vlen_uint64_data_chunked")),
				dynamicTest("int8", createTest(earliestHdfFile, "vlen_int8_data")),
				dynamicTest("int8 - chunked", createTest(earliestHdfFile, "vlen_int8_data_chunked")),
				dynamicTest("int16", createTest(earliestHdfFile, "vlen_int16_data")),
				dynamicTest("int16 - chunked", createTest(earliestHdfFile, "vlen_int16_data_chunked")),
				dynamicTest("int32", createTest(earliestHdfFile, "vlen_int32_data")),
				dynamicTest("int32 - chunked", createTest(earliestHdfFile, "vlen_int32_data_chunked")),
				dynamicTest("int64", createTest(earliestHdfFile, "vlen_int64_data")),
				dynamicTest("int64 - chunked", createTest(earliestHdfFile, "vlen_int64_data_chunked"))
			)),

			dynamicContainer(HDF5_TEST_LATEST_FILE_NAME, Arrays.asList(
				dynamicTest("float32", createTest(latestHdfFile, "vlen_float32_data")),
				dynamicTest("float32 - chunked", createTest(latestHdfFile, "vlen_float32_data_chunked")),
				dynamicTest("float64", createTest(latestHdfFile, "vlen_float64_data")),
				dynamicTest("float64 - chunked", createTest(latestHdfFile, "vlen_float64_data_chunked")),
				dynamicTest("uint8", createTest(latestHdfFile, "vlen_uint8_data")),
				dynamicTest("uint8 - chunked", createTest(latestHdfFile, "vlen_uint8_data_chunked")),
				dynamicTest("uint16", createTest(latestHdfFile, "vlen_uint16_data")),
				dynamicTest("uint16 - chunked", createTest(latestHdfFile, "vlen_uint16_data_chunked")),
				dynamicTest("uint32", createTest(latestHdfFile, "vlen_uint32_data")),
				dynamicTest("uint32 - chunked", createTest(latestHdfFile, "vlen_uint32_data_chunked")),
				dynamicTest("uint64", createTest(latestHdfFile, "vlen_uint64_data")),
				dynamicTest("uint64 - chunked", createTest(latestHdfFile, "vlen_uint64_data_chunked")),
				dynamicTest("int8", createTest(earliestHdfFile, "vlen_int8_data")),
				dynamicTest("int8 - chunked", createTest(earliestHdfFile, "vlen_int8_data_chunked")),
				dynamicTest("int16", createTest(earliestHdfFile, "vlen_int16_data")),
				dynamicTest("int16 - chunked", createTest(earliestHdfFile, "vlen_int16_data_chunked")),
				dynamicTest("int32", createTest(earliestHdfFile, "vlen_int32_data")),
				dynamicTest("int32 - chunked", createTest(earliestHdfFile, "vlen_int32_data_chunked")),
				dynamicTest("int64", createTest(earliestHdfFile, "vlen_int64_data")),
				dynamicTest("int64 - chunked", createTest(earliestHdfFile, "vlen_int64_data_chunked"))
			)));
	}

	private Executable createTest(HdfFile hdfFile, String datasetPath) {
		return () -> {
			Dataset dataset = hdfFile.getDatasetByPath(datasetPath);

			assertThat(dataset.isVariableLength(), is(true));
			assertThat(dataset.getDimensions(), is(new int[]{3}));
			assertThat(dataset.getJavaType(), is(Object.class));

			Object[] data = (Object[]) dataset.getData();

			// 3 elements of the data each is an array
			assertThat(data.length, is(3));
			// Now check the dimensions of each element
			assertThat(Array.getLength(data[0]), is(1));
			assertThat(Array.getLength(data[1]), is(2));
			assertThat(Array.getLength(data[2]), is(3));

			// Now check the values
			Object[] flatData = Utils.flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
		};
	}

	@Test // https://github.com/jamesmudd/jhdf/issues/247
	void issue247Earliest() {
		Dataset dataset = earliestHdfFile.getDatasetByPath("vlen_issue_247");

		assertThat(dataset.isVariableLength(), is(true));
		assertThat(dataset.getDimensions(), is(new int[]{3}));
		assertThat(dataset.getJavaType(), is(Object.class));

		Object[] data = (Object[]) dataset.getData();

		// 3 elements of the data each is an array
		assertThat(data.length, is(3));
		// Now check the dimensions of each element
		assertThat(toObject((int[]) data[0]), is(arrayContaining(1, 2, 3)));
		assertThat(toObject((int[]) data[1]), is(emptyArray()));
		assertThat(toObject((int[]) data[2]), is(arrayContaining(1, 2, 3, 4, 5)));
	}

	@Test
	void issue247Latest() {
		Dataset dataset = latestHdfFile.getDatasetByPath("vlen_issue_247");

		assertThat(dataset.isVariableLength(), is(true));
		assertThat(dataset.getDimensions(), is(new int[]{3}));
		assertThat(dataset.getJavaType(), is(Object.class));

		Object[] data = (Object[]) dataset.getData();

		// 3 elements of the data each is an array
		assertThat(data.length, is(3));
		// Now check the dimensions of each element
		assertThat(toObject((int[]) data[0]), is(arrayContaining(1, 2, 3)));
		assertThat(toObject((int[]) data[1]), is(emptyArray()));
		assertThat(toObject((int[]) data[2]), is(arrayContaining(1, 2, 3, 4, 5)));
	}
}
