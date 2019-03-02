package io.jhdf.dataset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

public class ScalarDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "../test_scalar_empty_datasets_latest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "../test_scalar_empty_datasets_earliest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = ScalarDatasetTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = ScalarDatasetTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> scalarDatasetTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicTest("earliest - float32",
						createTest(earliestHdfFile, "scalar_float_32", 123.45f)),
				dynamicTest("earliest - float64",
						createTest(earliestHdfFile, "scalar_float_64", 123.45d)),
				dynamicTest("earliest - int8",
						createTest(earliestHdfFile, "scalar_int_8", (byte) 123)),
				dynamicTest("earliest - int16",
						createTest(earliestHdfFile, "scalar_int_16", (short) 123)),
				dynamicTest("earliest - int32",
						createTest(earliestHdfFile, "scalar_int_32", 123)),
				dynamicTest("earliest - int64",
						createTest(earliestHdfFile, "scalar_int_64", (long) 123)),
				dynamicTest("earliest - uint8",
						createTest(earliestHdfFile, "scalar_uint_8", 123)),
				dynamicTest("earliest - uint16",
						createTest(earliestHdfFile, "scalar_uint_16", 123)),
				dynamicTest("earliest - uint32",
						createTest(earliestHdfFile, "scalar_uint_32", (long) 123)),
				dynamicTest("earliest - uint64",
						createTest(earliestHdfFile, "scalar_uint_64", BigInteger.valueOf(123))),
				dynamicTest("earliest - string",
						createTest(earliestHdfFile, "scalar_string", "hello")),

				dynamicTest("latest - float32",
						createTest(latestHdfFile, "scalar_float_32", 123.45f)),
				dynamicTest("latest - float64",
						createTest(latestHdfFile, "scalar_float_64", 123.45d)),
				dynamicTest("latest - int8",
						createTest(latestHdfFile, "scalar_int_8", (byte) 123)),
				dynamicTest("latest - int16",
						createTest(latestHdfFile, "scalar_int_16", (short) 123)),
				dynamicTest("latest - int32",
						createTest(latestHdfFile, "scalar_int_32", 123)),
				dynamicTest("latest - int64",
						createTest(latestHdfFile, "scalar_int_64", (long) 123)),
				dynamicTest("latest - uint8",
						createTest(latestHdfFile, "scalar_uint_8", 123)),
				dynamicTest("latest - uint16",
						createTest(latestHdfFile, "scalar_uint_16", 123)),
				dynamicTest("latest - uint32",
						createTest(latestHdfFile, "scalar_uint_32", (long) 123)),
				dynamicTest("latest - uint64",
						createTest(latestHdfFile, "scalar_uint_64", BigInteger.valueOf(123))),
				dynamicTest("latest - string",
						createTest(latestHdfFile, "scalar_string", "hello")));
	}

	private Executable createTest(HdfFile file, String datasetPath, Object expected) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			// should have 0 lentgh dimensions
			assertThat(dataset.getDimensions(), is(equalTo(new int[0])));
			assertThat(dataset.isScalar(), is(true));
			assertThat(dataset.getData(), is(equalTo(expected)));
		};
	}

}
