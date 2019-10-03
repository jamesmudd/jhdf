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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

public class ScalarDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "../test_scalar_empty_datasets_latest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "../test_scalar_empty_datasets_earliest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = ScalarDatasetTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = ScalarDatasetTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> scalarDatasetTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicContainer("earliest", Arrays.asList(
						dynamicTest("float32",
								createTest(earliestHdfFile, "scalar_float_32", 123.45f)),
						dynamicTest("float64",
								createTest(earliestHdfFile, "scalar_float_64", 123.45d)),
						dynamicTest("int8",
								createTest(earliestHdfFile, "scalar_int_8", (byte) 123)),
						dynamicTest("int16",
								createTest(earliestHdfFile, "scalar_int_16", (short) 123)),
						dynamicTest("int32",
								createTest(earliestHdfFile, "scalar_int_32", 123)),
						dynamicTest("int64",
								createTest(earliestHdfFile, "scalar_int_64", (long) 123)),
						dynamicTest("uint8",
								createTest(earliestHdfFile, "scalar_uint_8", 123)),
						dynamicTest("uint16",
								createTest(earliestHdfFile, "scalar_uint_16", 123)),
						dynamicTest("uint32",
								createTest(earliestHdfFile, "scalar_uint_32", (long) 123)),
						dynamicTest("uint64",
								createTest(earliestHdfFile, "scalar_uint_64", BigInteger.valueOf(123))),
						dynamicTest("string",
								createTest(earliestHdfFile, "scalar_string", "hello")))),

				dynamicContainer("latest", Arrays.asList(
						dynamicTest("float32",
								createTest(latestHdfFile, "scalar_float_32", 123.45f)),
						dynamicTest("float64",
								createTest(latestHdfFile, "scalar_float_64", 123.45d)),
						dynamicTest("int8",
								createTest(latestHdfFile, "scalar_int_8", (byte) 123)),
						dynamicTest("int16",
								createTest(latestHdfFile, "scalar_int_16", (short) 123)),
						dynamicTest("int32",
								createTest(latestHdfFile, "scalar_int_32", 123)),
						dynamicTest("int64",
								createTest(latestHdfFile, "scalar_int_64", (long) 123)),
						dynamicTest("uint8",
								createTest(latestHdfFile, "scalar_uint_8", 123)),
						dynamicTest("uint16",
								createTest(latestHdfFile, "scalar_uint_16", 123)),
						dynamicTest("uint32",
								createTest(latestHdfFile, "scalar_uint_32", (long) 123)),
						dynamicTest("uint64",
								createTest(latestHdfFile, "scalar_uint_64", BigInteger.valueOf(123))),
						dynamicTest("string",
								createTest(latestHdfFile, "scalar_string", "hello")))));
	}

	private Executable createTest(HdfFile file, String datasetPath, Object expected) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			// should have 0 length dimensions
			assertThat(dataset.getDimensions(), is(equalTo(new int[0])));
			assertThat(dataset.isScalar(), is(true));
			assertThat(dataset.getData(), is(equalTo(expected)));
			assertThat(dataset.getDiskSize(), is(greaterThan(0L)));
		};
	}

}
