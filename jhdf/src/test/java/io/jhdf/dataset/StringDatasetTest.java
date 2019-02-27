package io.jhdf.dataset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

public class StringDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "../test_string_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "../test_string_datasets_latest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = StringDatasetTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = StringDatasetTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
	}

	@TestFactory
	Collection<DynamicNode> stringDataset1DTests() {
		// List of all the datasetPaths
		return Arrays.asList(
				dynamicTest("earliest - fixed ASCII",
						createTest(earliestHdfFile, "/fixed_lentgh_ascii")),
				dynamicTest("earliest - variable ASCII",
						createTest(earliestHdfFile, "/variable_lentgh_ascii")),
				dynamicTest("earliest - variable UTF8",
						createTest(earliestHdfFile, "/variable_lentgh_utf8")),
				dynamicTest("latest - fixed ASCII",
						createTest(latestHdfFile, "/fixed_lentgh_ascii")),
				dynamicTest("latest - variable ASCII",
						createTest(latestHdfFile, "/variable_lentgh_ascii")),
				dynamicTest("latest - variable UTF8",
						createTest(latestHdfFile, "/variable_lentgh_utf8")));
	}

	private Executable createTest(HdfFile file, String datasetPath) {
		return () -> {
			Dataset dataset = file.getDatasetByPath(datasetPath);
			Object data = dataset.getData();
			assertThat(getDimensions(data), is(equalTo(new int[] { 10 })));
			Object[] flatData = flatten((Object[]) data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				// convert to double
				assertThat(flatData[i], is(equalTo("string number " + i)));
			}
		};
	}

	@Test
	void test2DStringDatasetEarliest() throws Exception {
		Dataset dataset = earliestHdfFile.getDatasetByPath("variable_lentgh_2d");
		Object data = dataset.getData();
		assertThat(getDimensions(data), is(equalTo(new int[] { 5, 7 })));
		Object[] flatData = flatten((Object[]) data);
		for (int i = 0; i < flatData.length; i++) {
			// Do element comparison as there are all different primitive numeric types
			// convert to double
			assertThat(flatData[i], is(equalTo(Integer.toString(i))));
		}
	}

	@Test
	void test2DStringDatasetLatest() throws Exception {
		Dataset dataset = latestHdfFile.getDatasetByPath("variable_lentgh_2d");
		Object data = dataset.getData();
		assertThat(getDimensions(data), is(equalTo(new int[] { 5, 7 })));
		Object[] flatData = flatten((Object[]) data);
		for (int i = 0; i < flatData.length; i++) {
			// Do element comparison as there are all different primitive numeric types
			// convert to double
			assertThat(flatData[i], is(equalTo(Integer.toString(i))));
		}
	}

	private Object[] flatten(Object[] data) {
		List<Object> flat = new ArrayList<>();
		flattenInternal(data, flat);
		return flat.toArray();
	}

	private void flattenInternal(Object data, List<Object> flat) {
		int length = Array.getLength(data);
		for (int i = 0; i < length; i++) {
			Object element = Array.get(data, i);
			if (element.getClass().isArray()) {
				flattenInternal(element, flat);
			} else {
				flat.add(element);
			}
		}
	}

	private int[] getDimensions(Object data) {
		List<Integer> dims = new ArrayList<>();
		dims.add(Array.getLength(data));

		while (Array.get(data, 0).getClass().isArray()) {
			data = Array.get(data, 0);
			dims.add(Array.getLength(data));
		}
		return ArrayUtils.toPrimitive(dims.toArray(new Integer[dims.size()]));
	}
}
