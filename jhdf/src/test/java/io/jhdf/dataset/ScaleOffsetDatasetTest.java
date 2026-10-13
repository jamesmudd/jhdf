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
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static io.jhdf.Utils.flatten;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ScaleOffsetDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_scale_offset_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_scale_offset_datasets_latest.hdf5";

	private static final int[] DIMENSIONS_2D = new int[]{7, 5};
	private static final int[] DIMENSIONS_1D = new int[]{32};

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		earliestHdfFile = loadTestHdfFile(HDF5_TEST_EARLIEST_FILE_NAME);
		latestHdfFile = loadTestHdfFile(HDF5_TEST_LATEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		earliestHdfFile.close();
		latestHdfFile.close();
	}

	@TestFactory
	Collection<DynamicNode> scaleOffsetDatasetReadTests() {
		return Arrays.asList(
			dynamicContainer(HDF5_TEST_EARLIEST_FILE_NAME, Arrays.asList(
				dynamicTest("int8", createTest(earliestHdfFile, "/int/int8", DIMENSIONS_2D)),
				dynamicTest("int16", createTest(earliestHdfFile, "/int/int16", DIMENSIONS_2D)),
				dynamicTest("int16_3", createTest(earliestHdfFile, "/int/int16_3", DIMENSIONS_2D)),
				dynamicTest("int32", createTest(earliestHdfFile, "/int/int32", DIMENSIONS_2D)),
				dynamicTest("int32_3", createTest(earliestHdfFile, "/int/int32_3", DIMENSIONS_2D)),
				dynamicTest("int_simple/int8", createTest(earliestHdfFile, "/int_simple/int8", DIMENSIONS_1D)),
				dynamicTest("int_simple/int16", createTest(earliestHdfFile, "/int_simple/int16", DIMENSIONS_1D)),
				dynamicTest("int_simple/int16_3", createTest(earliestHdfFile, "/int_simple/int16_3", DIMENSIONS_1D)),
				dynamicTest("int_simple/int32", createTest(earliestHdfFile, "/int_simple/int32", DIMENSIONS_1D)),
				dynamicTest("int_simple/int32_3", createTest(earliestHdfFile, "/int_simple/int32_3", DIMENSIONS_1D)))),

			dynamicContainer(HDF5_TEST_LATEST_FILE_NAME, Arrays.asList(
				dynamicTest("int8", createTest(latestHdfFile, "/int/int8", DIMENSIONS_2D)),
				dynamicTest("int16", createTest(latestHdfFile, "/int/int16", DIMENSIONS_2D)),
				dynamicTest("int16_3", createTest(latestHdfFile, "/int/int16_3", DIMENSIONS_2D)),
				dynamicTest("int32", createTest(latestHdfFile, "/int/int32", DIMENSIONS_2D)),
				dynamicTest("int32_3", createTest(latestHdfFile, "/int/int32_3", DIMENSIONS_2D)),
				dynamicTest("int_simple/int8", createTest(latestHdfFile, "/int_simple/int8", DIMENSIONS_1D)),
				dynamicTest("int_simple/int16", createTest(latestHdfFile, "/int_simple/int16", DIMENSIONS_1D)),
				dynamicTest("int_simple/int16_3", createTest(latestHdfFile, "/int_simple/int16_3", DIMENSIONS_1D)),
				dynamicTest("int_simple/int32", createTest(latestHdfFile, "/int_simple/int32", DIMENSIONS_1D)),
				dynamicTest("int_simple/int32_3", createTest(latestHdfFile, "/int_simple/int32_3", DIMENSIONS_1D))))
		);
	}

	private Executable createTest(HdfFile hdfFile, String datasetPath, int[] expectedDimensions) {
		return () -> {
			Dataset dataset = hdfFile.getDatasetByPath(datasetPath);
			Object data = dataset.getData();

			assertThat(Utils.getDimensions(data), is(equalTo(expectedDimensions)));

			Object[] flatData = flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
		};
	}
}
