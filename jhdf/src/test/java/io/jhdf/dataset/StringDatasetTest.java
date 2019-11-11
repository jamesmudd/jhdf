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
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.object.datatype.StringData;
import io.jhdf.object.datatype.VariableLength;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;

import static io.jhdf.TestUtils.flatten;
import static io.jhdf.TestUtils.getDimensions;
import static io.jhdf.TestUtils.loadTestHdfFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class StringDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_string_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_string_datasets_latest.hdf5";
	private static final String HDF5_UTF8_FIXED = "utf8-fixed-length.hdf5";
	private static final String HDF5_UTF8_VARIABLE_LENGTH_REUSED = "var-length-strings-reused.hdf5";
	private static final String HDF5_STRING_PADDED_DATASET = "space_padding_problem.hdf5";
	private static final String HDF5_MULTI_DIM_FIXED_LENGTH_STRING_DATASET = "multidim_string_datasest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;
	private static HdfFile utf8FixedHdfFile;
	private static HdfFile utf8VariableLengthReusedHdfFile;
	private static HdfFile stringPaddedHdfFile;
	private static HdfFile multiDimDatasetHdfFile;

	@BeforeAll
	static void setup() throws Exception {
		earliestHdfFile = loadTestHdfFile(HDF5_TEST_EARLIEST_FILE_NAME);
		latestHdfFile = loadTestHdfFile(HDF5_TEST_LATEST_FILE_NAME);
		utf8FixedHdfFile = loadTestHdfFile(HDF5_UTF8_FIXED);
		utf8VariableLengthReusedHdfFile = loadTestHdfFile(HDF5_UTF8_VARIABLE_LENGTH_REUSED);
		stringPaddedHdfFile = loadTestHdfFile(HDF5_STRING_PADDED_DATASET);
		multiDimDatasetHdfFile = loadTestHdfFile(HDF5_MULTI_DIM_FIXED_LENGTH_STRING_DATASET);
	}

	@AfterAll
	static void tearDown() {
		earliestHdfFile.close();
		latestHdfFile.close();
		utf8FixedHdfFile.close();
		utf8VariableLengthReusedHdfFile.close();
		stringPaddedHdfFile.close();
		multiDimDatasetHdfFile.close();
	}

	@TestFactory
	Collection<DynamicNode> stringDataset1DTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicContainer("earliest", Arrays.asList(
						dynamicTest("fixed ASCII",
								createTest(earliestHdfFile, "/fixed_length_ascii")),
						dynamicTest("fixed ASCII 1 char",
								createTest(earliestHdfFile, "/fixed_length_ascii_1_char")),
						dynamicTest("variable ASCII",
								createTest(earliestHdfFile, "/variable_length_ascii")),
						dynamicTest("variable UTF8",
								createTest(earliestHdfFile, "/variable_length_utf8")))),

				dynamicContainer("latest", Arrays.asList(
						dynamicTest("fixed ASCII",
								createTest(latestHdfFile, "/fixed_length_ascii")),
						dynamicTest("fixed ASCII 1 char",
								createTest(latestHdfFile, "/fixed_length_ascii_1_char")),
						dynamicTest("variable ASCII",
								createTest(latestHdfFile, "/variable_length_ascii")),
						dynamicTest("variable UTF8",
								createTest(latestHdfFile, "/variable_length_utf8")))));
	}

	private Executable createTest(HdfFile file, String datasetPath) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(getDimensions(data), is(equalTo(new int[] { 10 })));
			Object[] flatData = flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(flatData[i], is(equalTo("string number " + i)));
			}
		};
	}

	@Test
	void test2DStringDatasetEarliest() {
		Dataset dataset = earliestHdfFile.getDatasetByPath("variable_length_2d");
		Object data = dataset.getData();
		assertThat(getDimensions(data), is(equalTo(new int[] { 5, 7 })));
		Object[] flatData = flatten(data);
		for (int i = 0; i < flatData.length; i++) {
			// Do element comparison as there are all different primitive numeric types
			// convert to double
			assertThat(flatData[i], is(equalTo(Integer.toString(i))));
		}
	}

	@Test
	void test2DStringDatasetLatest() {
		Dataset dataset = latestHdfFile.getDatasetByPath("variable_length_2d");
		Object data = dataset.getData();
		assertThat(getDimensions(data), is(equalTo(new int[] { 5, 7 })));
		Object[] flatData = flatten(data);
		for (int i = 0; i < flatData.length; i++) {
			// Do element comparison as there are all different primitive numeric types
			// convert to double
			assertThat(flatData[i], is(equalTo(Integer.toString(i))));
		}
	}

	@Test // https://github.com/jamesmudd/jhdf/issues/113
	void testUtf8FixedLengthDataset() {
		Dataset dataset = utf8FixedHdfFile.getDatasetByPath("a0");

		assertThat(dataset.getDimensions(), is(equalTo(new int[]{ 10 })));

		StringData stringDataType = (StringData) dataset.getDataType();
		assertThat(stringDataType.getCharset(), is(sameInstance(UTF_8)));

		Object data = dataset.getData();
		String[] stringData = (String[]) data;
		assertThat(stringData, is(arrayContaining(
				"att-1ä@µÜß?3",
				"att-1ä@µÜß?1",
				"att-1ä@µÜß?0",
				"att-1ä@µÜß?0",
				"att-1ä@µÜß?0",
				"att-1ä@µÜß?6",
				"att-1ä@µÜß?2",
				"att-1ä@µÜß?5",
				"att-1ä@µÜß?0",
				"att-1ä@µÜß?5")));
	}

	@Test // https://github.com/jamesmudd/jhdf/issues/113
	void testUtf8VariableLengthReusedDataset() {
		Dataset dataset = utf8VariableLengthReusedHdfFile.getDatasetByPath("a0");

		assertThat(dataset.getDimensions(), is(equalTo(new int[]{ 10 })));

		VariableLength variableLengthDataType = (VariableLength) dataset.getDataType();
		assertThat(variableLengthDataType.getEncoding(), is(sameInstance(UTF_8)));

		Object data = dataset.getData();
		String[] stringData = (String[]) data;
		assertThat(stringData, is(arrayContaining(
				"att-0-value-1",
				"att-0-value-1",
				"NULL",
				"NULL",
				"NULL",
				"att-0-value-1",
				"att-0-value-0",
				"att-0-value-1",
				"NULL",
				"NULL")));
	}

	@Test // https://github.com/jamesmudd/jhdf/issues/124
	void testStringPaddedDataset() {
		Attribute attribute = stringPaddedHdfFile.getAttribute("Test");
		Object data = attribute.getData();
		String[] stringData = (String[]) data;
		assertThat(stringData, is(arrayContaining("a")));
	}

	@Test
	void testMultiDimensionalFixedLengthDataset() {
		Dataset dataset = multiDimDatasetHdfFile.getDatasetByPath("test");
		assertThat(toObject(dataset.getDimensions()), is(arrayContaining(3, 2)));

		Object data = dataset.getData();
		assertThat(toObject(getDimensions(data)), is(arrayContaining(3, 2)));

		Object[] flatData = flatten(data);
		assertThat(flatData, is(arrayContaining(
				"a1", "a2",
				"a3", "a4",
				"a5", "a6")));
	}
}
