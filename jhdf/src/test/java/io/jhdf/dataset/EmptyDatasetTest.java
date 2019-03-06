package io.jhdf.dataset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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

public class EmptyDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "../test_scalar_empty_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "../test_scalar_empty_datasets_latest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = EmptyDatasetTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = EmptyDatasetTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> emptyDatasetTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicTest("earliest - float32",
						createTest(earliestHdfFile, "empty_float_32", Float.class)),
				dynamicTest("earliest - float64",
						createTest(earliestHdfFile, "empty_float_64", Double.class)),
				dynamicTest("earliest - int8",
						createTest(earliestHdfFile, "empty_int_8", Byte.class)),
				dynamicTest("earliest - int16",
						createTest(earliestHdfFile, "empty_int_16", Short.class)),
				dynamicTest("earliest - int32",
						createTest(earliestHdfFile, "empty_int_32", Integer.class)),
				dynamicTest("earliest - int64",
						createTest(earliestHdfFile, "empty_int_64", Long.class)),
				dynamicTest("earliest - uint8",
						createTest(earliestHdfFile, "empty_uint_8", Integer.class)),
				dynamicTest("earliest - uint16",
						createTest(earliestHdfFile, "empty_uint_16", Integer.class)),
				dynamicTest("earliest - uint32",
						createTest(earliestHdfFile, "empty_uint_32", Long.class)),
				dynamicTest("earliest - uint64",
						createTest(earliestHdfFile, "empty_uint_64", BigInteger.class)),
				dynamicTest("earliest - string",
						createTest(earliestHdfFile, "empty_string", String.class)),

				dynamicTest("latest - float32",
						createTest(latestHdfFile, "empty_float_32", Float.class)),
				dynamicTest("latest - float64",
						createTest(latestHdfFile, "empty_float_64", Double.class)),
				dynamicTest("latest - int8",
						createTest(latestHdfFile, "empty_int_8", Byte.class)),
				dynamicTest("latest - int16",
						createTest(latestHdfFile, "empty_int_16", Short.class)),
				dynamicTest("latest - int32",
						createTest(latestHdfFile, "empty_int_32", Integer.class)),
				dynamicTest("latest - int64",
						createTest(latestHdfFile, "empty_int_64", Long.class)),
				dynamicTest("latest - uint8",
						createTest(latestHdfFile, "empty_uint_8", Integer.class)),
				dynamicTest("latest - uint16",
						createTest(latestHdfFile, "empty_uint_16", Integer.class)),
				dynamicTest("latest - uint32",
						createTest(latestHdfFile, "empty_uint_32", Long.class)),
				dynamicTest("latest - uint64",
						createTest(latestHdfFile, "empty_uint_64", BigInteger.class)),
				dynamicTest("latest - string",
						createTest(latestHdfFile, "empty_string", String.class)));
	}

	private Executable createTest(HdfFile file, String datasetPath, Class<?> expectedType) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			// should have 0 length dimensions
			assertThat(dataset.getDimensions(), is(equalTo(new int[0])));

			// Should be empty
			assertThat(dataset.isEmpty(), is(true));
			// Empty dataset getData should return null
			assertThat(dataset.getData(), is(nullValue()));

			assertThat(dataset.getJavaType(), is(equalTo(expectedType)));
		};
	}

}
