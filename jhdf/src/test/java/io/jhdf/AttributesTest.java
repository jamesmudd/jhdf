/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class AttributesTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_attribute_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_attribute_latest.hdf5";

	private static final String GROUP_PATH = "/test_group";
	private static final String DATASET_PATH = "/test_group/data";
	private static final String HARD_LINK_PATH = "/hard_link_data";
	private static final String SOFT_LINK_PATH = "/soft_link_to_data";

	private static final String ONE_D_FLOAT_ATTRIBUTE_NAME = "1D_float";
	private static final float[] EXPECTED_ONE_D_FLOAT_DATA = new float[] { 0, 1, 2 };

	private static final String ONE_D_INT_ATTRIBUTE_NAME = "1D_int";
	private static final int[] EXPECTED_ONE_D_INT_DATA = new int[] { 0, 1, 2 };

	private static final String TWO_D_FLOAT_ATTRIBUTE_NAME = "2D_float";
	private static final float[][] EXPECTED_TWO_D_FLOAT_DATA = new float[][] { { 0, 1, 2 }, { 3, 4, 5 } };

	private static final String TWO_D_INT_ATTRIBUTE_NAME = "2D_int";
	private static final int[][] EXPECTED_TWO_D_INT_DATA = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };

	private static final String TWO_D_STRING_ATTRIBUTE_NAME = "2d_string";
	private static final String[][] EXPECTED_TWO_D_STRING_DATA = new String[][] { { "0", "1", "2" },
			{ "3", "4", "5" } };

	private static final String EMPTY_FLOAT_ATTRIBUTE_NAME = "empty_float";
	private static final String EMPTY_INT_ATTRIBUTE_NAME = "empty_int";
	private static final String EMPTY_STRING_ATTRIBUTE_NAME = "empty_string";
	private static final Object EXPECTED_EMPTY_DATA = null;

	private static final String SCALAR_FLOAT_ATTRIBUTE_NAME = "scalar_float";
	private static final Float EXPECTED_SCALAR_FLOAT_DATA = 123.45f;
	private static final String SCALAR_INT_ATTRIBUTE_NAME = "scalar_int";
	private static final Integer EXPECTED_SCALAR_INT_DATA = 123;
	private static final String SCALAR_STRING_ATTRIBUTE_NAME = "scalar_string";
	private static final String EXPECTED_SCALAR_STRING_DATA = "hello";

	private static final String SCALAR_REFERENCE_ATTRIBUTE_NAME = "object_reference";
	private static final long EXPECTED_SCALAR_REFERENCE_DATA_EARLIEST = 96;
	private static final long EXPECTED_SCALAR_REFERENCE_DATA_LATEST = 48;

	private static final String ONE_D_REFERENCE_ATTRIBUTE_NAME = "1D_object_references";
	private static final long[] EXPECTED_1D_REFERENCE_DATA_EARLIEST = {96, 800};
	private static final long[] EXPECTED_1D_REFERENCE_DATA_LATEST = {48, 195};

	private static final String TWO_D_REFERENCE_ATTRIBUTE_NAME = "2D_object_references";
	private static final long[][] EXPECTED_2D_REFERENCE_DATA_EARLIEST = {EXPECTED_1D_REFERENCE_DATA_EARLIEST,
			EXPECTED_1D_REFERENCE_DATA_EARLIEST};
	private static final long[][] EXPECTED_2D_REFERENCE_DATA_LATEST = {EXPECTED_1D_REFERENCE_DATA_LATEST,
			EXPECTED_1D_REFERENCE_DATA_LATEST};


	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = AttributesTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = AttributesTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> attributeTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicContainer("earliest", Arrays.asList(

						// Group
						dynamicContainer(GROUP_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, GROUP_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_EARLIEST)))),

						// Dataset
						dynamicContainer(DATASET_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, DATASET_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_EARLIEST)))),

						// Hard link
						dynamicContainer(HARD_LINK_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, HARD_LINK_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_EARLIEST)))),


						// Soft link
						dynamicContainer(SOFT_LINK_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_EARLIEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(earliestHdfFile, SOFT_LINK_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_EARLIEST))))

				)),
				dynamicContainer("latest", Arrays.asList(

						// Group
						dynamicContainer(GROUP_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_LATEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_LATEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, GROUP_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_LATEST)))),

						// Dataset
						dynamicContainer(DATASET_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_LATEST)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_LATEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_LATEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, DATASET_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_LATEST)))),

						// Hard link
						dynamicContainer(HARD_LINK_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_LATEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_LATEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, HARD_LINK_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_LATEST)))),

						// Soft link
						dynamicContainer(SOFT_LINK_PATH, Arrays.asList(
								dynamicTest(ONE_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, ONE_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_FLOAT_DATA)),
								dynamicTest(ONE_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, ONE_D_INT_ATTRIBUTE_NAME,
												EXPECTED_ONE_D_INT_DATA)),
								dynamicTest(TWO_D_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, TWO_D_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_FLOAT_DATA)),
								dynamicTest(TWO_D_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, TWO_D_INT_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_INT_DATA)),
								dynamicTest(TWO_D_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, TWO_D_STRING_ATTRIBUTE_NAME,
												EXPECTED_TWO_D_STRING_DATA)),
								dynamicTest(EMPTY_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, EMPTY_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, EMPTY_INT_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(EMPTY_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, EMPTY_STRING_ATTRIBUTE_NAME,
												EXPECTED_EMPTY_DATA)),
								dynamicTest(SCALAR_FLOAT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, SCALAR_FLOAT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_FLOAT_DATA)),
								dynamicTest(SCALAR_INT_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, SCALAR_INT_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_INT_DATA)),
								dynamicTest(SCALAR_STRING_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, SCALAR_STRING_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_STRING_DATA)),
								dynamicTest(TWO_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, TWO_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_2D_REFERENCE_DATA_LATEST)),
								dynamicTest(ONE_D_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, ONE_D_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_1D_REFERENCE_DATA_LATEST)),
								dynamicTest(SCALAR_REFERENCE_ATTRIBUTE_NAME,
										createTest(latestHdfFile, SOFT_LINK_PATH, SCALAR_REFERENCE_ATTRIBUTE_NAME,
												EXPECTED_SCALAR_REFERENCE_DATA_LATEST))))

				)));
	}

	private Executable createTest(HdfFile file, String nodePath, String attributeName, Object expectedData) {
		return () -> {
			Node node = file.getByPath(nodePath);
			Attribute attribute = node.getAttribute(attributeName);

			assertThat(attribute.getData(), is(equalTo(expectedData)));
			// Call getData again to ensure the is no persisted state after the first read.
			assertThat(attribute.getData(), is(equalTo(expectedData)));

			assertThat(attribute.getName(), is(equalTo(attributeName)));

			if (expectedData == null) {// Empty attributes
				assertThat(attribute.isEmpty(), is(true));
				assertThat(attribute.isScalar(), is(false));
				assertThat(attribute.getSize(), is(0L));
				assertThat(attribute.getDiskSize(), is(0L));
			} else if (expectedData.getClass().isArray()) { // Array
				assertThat(attribute.getJavaType(), is(equalTo(getArrayType(expectedData))));
				assertThat(attribute.isEmpty(), is(false));
				assertThat(attribute.isScalar(), is(false));
			} else { // Scalar
				assertThat(attribute.getJavaType(), is(equalTo(expectedData.getClass())));
				assertThat(attribute.isEmpty(), is(false));
				assertThat(attribute.isScalar(), is(true));
				assertThat(attribute.getSize(), is(1L));
			}

			if (!node.isLink()) {
				assertThat(attribute.getNode(), is(sameInstance(node)));
			}
		};
	}

	Class<?> getArrayType(Object array) {
		Object element = Array.get(array, 0);
		if (element.getClass().isArray()) {
			return getArrayType(element);
		} else {
			return array.getClass().getComponentType();
		}
	}

	@Test
	void testAttributeCreationOrderTrackingEarliest() {
		// Earliest doesn't support attribute tracking
		assertThat(earliestHdfFile.getByPath(DATASET_PATH).isAttributeCreationOrderTracked(), is(false));
		assertThat(earliestHdfFile.getByPath(SOFT_LINK_PATH).isAttributeCreationOrderTracked(), is(false));
		assertThat(earliestHdfFile.isAttributeCreationOrderTracked(), is(false));
	}

	@Test
	void testAttributeCreationOrderTrackingLatest() {
		assertThat(latestHdfFile.getByPath(DATASET_PATH).isAttributeCreationOrderTracked(), is(false));
		assertThat(latestHdfFile.getByPath(SOFT_LINK_PATH).isAttributeCreationOrderTracked(), is(false));
		assertThat(latestHdfFile.isAttributeCreationOrderTracked(), is(false));
	}
}
