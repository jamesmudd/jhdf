/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.TestUtils;
import io.jhdf.api.Dataset;
import io.jhdf.filter.PipelineFilterWithData;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.jhdf.TestUtils.flatten;
import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ByteShuffleChunkedDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_byteshuffle_compressed_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_byteshuffle_compressed_datasets_latest.hdf5";

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
				dynamicTest("float32", createTest(earliestHdfFile, "/float/float32", 4)),
				dynamicTest("float64", createTest(earliestHdfFile, "/float/float64", 9)),
				dynamicTest("int8", createTest(earliestHdfFile, "/int/int8", 4)),
				dynamicTest("int16", createTest(earliestHdfFile, "/int/int16", 1)),
				dynamicTest("int32", createTest(earliestHdfFile, "/int/int32", 7)))),

			dynamicContainer(HDF5_TEST_LATEST_FILE_NAME, Arrays.asList(
				dynamicTest("float32", createTest(latestHdfFile, "/float/float32", 4)),
				dynamicTest("float64", createTest(latestHdfFile, "/float/float64", 9)),
				dynamicTest("int8", createTest(latestHdfFile, "/int/int8", 4)),
				dynamicTest("int16", createTest(latestHdfFile, "/int/int16",1)),
				dynamicTest("int32", createTest(latestHdfFile, "/int/int32", 7)))));
	}

	private Executable createTest(HdfFile hdfFile, String datasetPath, int compressionLevel) {
		return () -> {
			Dataset dataset = hdfFile.getDatasetByPath(datasetPath);

			// Check filters
			List<PipelineFilterWithData> filters = dataset.getFilters();
			assertThat(filters,hasSize(2));
			assertThat(filters.get(0).getName(), is("shuffle"));
			assertThat(filters.get(0).getId(), is(2));
			assertThat(ArrayUtils.toObject(filters.get(0).getFilterData()), is(arrayContaining(dataset.getDataType().getSize())));
			assertThat(filters.get(1).getName(), is("deflate"));
			assertThat(filters.get(1).getId(), is(1));
			assertThat(ArrayUtils.toObject(filters.get(1).getFilterData()), is(arrayContaining(compressionLevel)));

			Object data = dataset.getData();
			assertThat(TestUtils.getDimensions(data), is(equalTo(new int[]{7, 5})));
			Object[] flatData = flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
		};
	}

}
