/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.attribute;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

// https://github.com/jamesmudd/jhdf/issues/338
class CompoundScalarAttributeTest {

	private static final String HDF5_TEST_FILE_NAME = "test_compound_scalar_attribute.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		hdfFile.close();
	}

	@Test
	void testReadAttribute() {

		Node node = hdfFile.getByPath("GROUP");
		Attribute attribute = node.getAttribute("VERSION");

		Object data = attribute.getData();

		Map<String, int[]> dataAsMap = (Map<String, int[]>) data;

		String key = "myMajor";
		assertThat(dataAsMap, hasKey(key));
		assertThat(dataAsMap.get(key)[0], is(1));

		key = "myMinor";
		assertThat(dataAsMap, hasKey(key));
		assertThat(dataAsMap.get(key)[0], is(0));

		key = "myPatch";
		assertThat(dataAsMap, hasKey(key));
		assertThat(dataAsMap.get(key)[0], is(0));
	}
}
