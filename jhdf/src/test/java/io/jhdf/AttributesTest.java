package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import io.jhdf.api.Attribute;
import io.jhdf.api.Node;

public class AttributesTest {

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
	private static final Float EXPECTED_SCALAR_FLOAT_DATA = Float.valueOf(123.45f);
	private static final String SCALAR_INT_ATTRIBUTE_NAME = "scalar_int";
	private static final Integer EXPECTED_SCALAR_INT_DATA = Integer.valueOf(123);
	private static final String SCALAR_STRING_ATTRIBUTE_NAME = "scalar_string";
	private static final String EXPECTED_SCALAR_STRING_DATA = "hello";

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
												EXPECTED_SCALAR_STRING_DATA)))),

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
												EXPECTED_SCALAR_STRING_DATA)))),

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
												EXPECTED_SCALAR_STRING_DATA)))),

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
												EXPECTED_SCALAR_STRING_DATA))))

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
												EXPECTED_SCALAR_STRING_DATA)))),

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
												EXPECTED_SCALAR_STRING_DATA)))),

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
												EXPECTED_SCALAR_STRING_DATA)))),

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
												EXPECTED_SCALAR_STRING_DATA))))

				)));
	}

	private Executable createTest(HdfFile file, String nodePath, String attributeName, Object expectedData) {
		return () -> {
			Node node = file.getByPath(nodePath);
			Attribute attribute = node.getAttribute(attributeName);

			assertThat(attribute.getData(), is(equalTo(expectedData)));

		};
	}

}
