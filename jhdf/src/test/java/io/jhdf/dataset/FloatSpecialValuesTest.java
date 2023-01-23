/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class FloatSpecialValuesTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "float_special_values_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "float_special_values_latest.hdf5";

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
	Collection<DynamicNode> specialFloatValuesTests() {
		// List of all the datasetPaths
		return Arrays.asList(
			dynamicContainer("earliest", Arrays.asList(
				dynamicTest("float16",
					testFloat(earliestHdfFile, "float16")),
				dynamicTest("float32",
					testFloat(earliestHdfFile, "float32")),
				dynamicTest("float64",
					testDouble(earliestHdfFile, "float64")))),

			dynamicContainer("latest", Arrays.asList(
				dynamicTest("float16",
					testFloat(latestHdfFile, "float16")),
				dynamicTest("float32",
					testFloat(latestHdfFile, "float32")),
				dynamicTest("float64",
					testDouble(latestHdfFile, "float64")))));
	}

	private Executable testDouble(HdfFile file, String datasetPath) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(Array.get(data, 0), is(Double.POSITIVE_INFINITY));
			assertThat(Array.get(data, 1), is(Double.NEGATIVE_INFINITY));
			assertThat(Array.get(data, 2), is(Double.NaN));
			assertThat(Array.get(data, 3), is(0d));
			assertThat(Array.get(data, 4), is(-0d));
		};
	}

	private Executable testFloat(HdfFile file, String datasetPath) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(Array.get(data, 0), is(Float.POSITIVE_INFINITY));
			assertThat(Array.get(data, 1), is(Float.NEGATIVE_INFINITY));
			assertThat(Array.get(data, 2), is(Float.NaN));
			assertThat(Array.get(data, 3), is(0f));
			assertThat(Array.get(data, 4), is(-0f));
		};
	}
}
