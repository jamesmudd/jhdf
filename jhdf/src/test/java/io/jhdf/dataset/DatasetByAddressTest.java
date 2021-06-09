/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Dataset;
import io.jhdf.storage.HdfBackingStorage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static io.jhdf.TestUtils.flatten;
import static io.jhdf.TestUtils.getDimensions;
import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;


class DatasetByAddressTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_string_datasets_earliest.hdf5";

	private static HdfFile earliestHdfFile;

	@BeforeAll
	static void setup() throws Exception {
		earliestHdfFile = loadTestHdfFile(HDF5_TEST_EARLIEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		earliestHdfFile.close();
	}

	@TestFactory
	Collection<DynamicNode> scalarDatasetTests() {
		// List of all the datasetPaths
		return Collections.singletonList(
			dynamicContainer("earliest", Arrays.asList(
				dynamicTest("fixed ASCII",
					createTest(earliestHdfFile, 800)),
				dynamicTest("fixed ASCII 1 char",
					createTest(earliestHdfFile, 1400)),
				dynamicTest("variable ASCII",
					createTest(earliestHdfFile, 1672)),
				dynamicTest("variable UTF8",
					createTest(earliestHdfFile, 6654)))));

	}

	private Executable createTest(HdfFile file, long address) {
		return () -> {
			HdfBackingStorage hdfBackingStorage = file.getHdfBackingStorage();
			ObjectHeader header = ObjectHeader.readObjectHeader(hdfBackingStorage, address);
			Dataset dataset = DatasetLoader.createDataset(hdfBackingStorage, header, "unknown dataset", NoParent.INSTANCE);
			Object data = dataset.getData();
			assertThat(getDimensions(data), is(equalTo(new int[]{10})));
			Object[] flatData = flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(flatData[i], is(equalTo("string number " + i)));
			}
		};
	}
}
