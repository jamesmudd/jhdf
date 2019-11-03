/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class EmptyDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_scalar_empty_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_scalar_empty_datasets_latest.hdf5";

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
	Collection<DynamicNode> emptyDatasetTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicContainer("earliest", Arrays.asList(
						dynamicTest("float32",
								createTest(earliestHdfFile, "empty_float_32", Float.class)),
						dynamicTest("float64",
								createTest(earliestHdfFile, "empty_float_64", Double.class)),
						dynamicTest("int8",
								createTest(earliestHdfFile, "empty_int_8", Byte.class)),
						dynamicTest("int16",
								createTest(earliestHdfFile, "empty_int_16", Short.class)),
						dynamicTest("int32",
								createTest(earliestHdfFile, "empty_int_32", Integer.class)),
						dynamicTest("int64",
								createTest(earliestHdfFile, "empty_int_64", Long.class)),
						dynamicTest("uint8",
								createTest(earliestHdfFile, "empty_uint_8", Integer.class)),
						dynamicTest("uint16",
								createTest(earliestHdfFile, "empty_uint_16", Integer.class)),
						dynamicTest("uint32",
								createTest(earliestHdfFile, "empty_uint_32", Long.class)),
						dynamicTest("uint64",
								createTest(earliestHdfFile, "empty_uint_64", BigInteger.class)),
						dynamicTest("string",
								createTest(earliestHdfFile, "empty_string", String.class)))),

				dynamicContainer("latest", Arrays.asList(
						dynamicTest("float32",
								createTest(latestHdfFile, "empty_float_32", Float.class)),
						dynamicTest("float64",
								createTest(latestHdfFile, "empty_float_64", Double.class)),
						dynamicTest("int8",
								createTest(latestHdfFile, "empty_int_8", Byte.class)),
						dynamicTest("int16",
								createTest(latestHdfFile, "empty_int_16", Short.class)),
						dynamicTest("int32",
								createTest(latestHdfFile, "empty_int_32", Integer.class)),
						dynamicTest("int64",
								createTest(latestHdfFile, "empty_int_64", Long.class)),
						dynamicTest("uint8",
								createTest(latestHdfFile, "empty_uint_8", Integer.class)),
						dynamicTest("uint16",
								createTest(latestHdfFile, "empty_uint_16", Integer.class)),
						dynamicTest("uint32",
								createTest(latestHdfFile, "empty_uint_32", Long.class)),
						dynamicTest("uint64",
								createTest(latestHdfFile, "empty_uint_64", BigInteger.class)),
						dynamicTest("string",
								createTest(latestHdfFile, "empty_string", String.class)))));
	}

	private Executable createTest(HdfFile file, String datasetPath, Class<?> expectedType) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			// should have 0 length dimensions
			assertThat(dataset.getDimensions(), is(equalTo(new int[0])));
			assertThat(dataset.getDiskSize(), is(equalTo(0L)));
			// Should be empty
			assertThat(dataset.isEmpty(), is(true));
			// Empty dataset getData should return null
			assertThat(dataset.getData(), is(nullValue()));

			assertThat(dataset.getJavaType(), is(equalTo(expectedType)));
		};
	}

}
