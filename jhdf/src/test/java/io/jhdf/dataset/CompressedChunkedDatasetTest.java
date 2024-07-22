/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class CompressedChunkedDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_compressed_chunked_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_compressed_chunked_datasets_latest.hdf5";

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
		List<DynamicTest> datasets = Arrays.asList(
			dynamicTest("float32", createTest(earliestHdfFile, "/float/float32", 0.456)),
			dynamicTest("float32lzf", createTest(earliestHdfFile, "/float/float32lzf", 0.875)),
			dynamicTest("float64", createTest(earliestHdfFile, "/float/float64", 1.637)),
			dynamicTest("float64lzf", createTest(earliestHdfFile, "/float/float64lzf", 1.379)),
			dynamicTest("int8", createTest(earliestHdfFile, "/int/int8", 0.454)),
			dynamicTest("int8lzf", createTest(earliestHdfFile, "/int/int8lzf", 0.636)),
			dynamicTest("int16", createTest(earliestHdfFile, "/int/int16", 0.2)),
			dynamicTest("int16lzf", createTest(earliestHdfFile, "/int/int16lzf", 1.0)),
			dynamicTest("int32", createTest(earliestHdfFile, "/int/int32", 0.625)),
			dynamicTest("int32lzf", createTest(earliestHdfFile, "/int/int32lzf", 0.833)));

		return Arrays.asList(
			dynamicContainer(HDF5_TEST_EARLIEST_FILE_NAME, datasets),
			dynamicContainer(HDF5_TEST_LATEST_FILE_NAME, datasets));
	}

	private Executable createTest(HdfFile hdfFile, String datasetPath, double expectedCompressionRatio) {
		return () -> {
			Dataset dataset = hdfFile.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(Utils.getDimensions(data), is(equalTo(new int[]{7, 5})));
			Object[] flatData = Utils.flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
			double actualCompressionRatio = (double) dataset.getSizeInBytes() / dataset.getStorageInBytes();
			assertThat(actualCompressionRatio, is(closeTo(expectedCompressionRatio, 0.01)));
		};
	}

}
