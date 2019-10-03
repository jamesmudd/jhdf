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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class FillValueDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "../test_fill_value_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "../test_fill_value_latest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = FillValueDatasetTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = FillValueDatasetTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> fillValueTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicContainer("earliest", Arrays.asList(
						dynamicTest("float32",
								createTest(earliestHdfFile, "float/float32", float.class, 33.33f)),
						dynamicTest("float64",
								createTest(earliestHdfFile, "float/float64", double.class, 123.456)),
						dynamicTest("int8",
								createTest(earliestHdfFile, "int/int8", byte.class, (byte) 8)),
						dynamicTest("int16",
								createTest(earliestHdfFile, "int/int16", short.class, (short) 16)),
						dynamicTest("int32",
								createTest(earliestHdfFile, "int/int32", int.class, 32)),
						dynamicTest("int64",
								createTest(earliestHdfFile, "no_fill", byte.class, null)))),

				dynamicContainer("latest", Arrays.asList(
						dynamicTest("float32",
								createTest(latestHdfFile, "float/float32", float.class, 33.33f)),
						dynamicTest("float64",
								createTest(latestHdfFile, "float/float64", double.class, 123.456)),
						dynamicTest("int8",
								createTest(latestHdfFile, "int/int8", byte.class, (byte) 8)),
						dynamicTest("int16",
								createTest(latestHdfFile, "int/int16", short.class, (short) 16)),
						dynamicTest("int32",
								createTest(latestHdfFile, "int/int32", int.class, 32)),
						dynamicTest("int64",
								createTest(latestHdfFile, "no_fill", byte.class, null)))));
	}

	private Executable createTest(HdfFile file, String datasetPath, Class<?> expectedType, Object fillValue) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			// should have 0 length dimensions
			assertThat(dataset.getDimensions(), is(equalTo(new int[] { 2, 5 })));

			// Do element comparison as there are all different primitive numeric types
			// convert to double
			assertThat(dataset.getFillValue(), is(equalTo(fillValue)));

			assertThat(dataset.getJavaType(), is(equalTo(expectedType)));
		};
	}

}
