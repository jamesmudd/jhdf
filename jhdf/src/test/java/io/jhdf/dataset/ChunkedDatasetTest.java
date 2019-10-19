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
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.jhdf.TestUtils.flatten;
import static io.jhdf.TestUtils.getDimensions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ChunkedDatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "../test_chunked_datasets_earliest.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() {
		String testFileUrl = ChunkedDatasetTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();
		hdfFile = new HdfFile(new File(testFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> chunkedDatasetReadTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicTest("float32", createTest("/float/float32")),
				dynamicTest("float64", createTest("/float/float64")),
				dynamicTest("int8", createTest("/int/int8")),
				dynamicTest("int16", createTest("/int/int16")),
				dynamicTest("int32", createTest("/int/int32")));
	}

	private Executable createTest(String datasetPath) {
		return () -> {
			Dataset dataset = hdfFile.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(getDimensions(data), is(equalTo(new int[] { 7, 5, 3 })));
			Object[] flatData = flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
		};
	}

}
