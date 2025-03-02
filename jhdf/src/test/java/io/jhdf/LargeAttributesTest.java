/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Attribute;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class LargeAttributesTest {
	private static final String HDF5_TEST_FILE_NAME = "test_large_attribute.hdf5";
	private static final String ATTRIBUTE_NAME = "large_attribute";
	private static final int EXPECTED_DATA_LENGTH = 8200;

	private static HdfFile largeAttrHdfFile;

	@BeforeAll
	static void setup() {
		largeAttrHdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		largeAttrHdfFile.close();
	}

	@Test
	void testGettingObject() {
		Attribute lattr = largeAttrHdfFile.getAttribute(ATTRIBUTE_NAME);
		assertThat(lattr, notNullValue());
		assertThat(lattr.getJavaType(), is(double.class));

		ByteBuffer bb = lattr.getBuffer();
		DoubleBuffer db = bb.asDoubleBuffer();
		assertThat(db.limit(), is(equalTo(EXPECTED_DATA_LENGTH)));

		double[] referenceData = new double[EXPECTED_DATA_LENGTH];
		for (int i = 0; i < EXPECTED_DATA_LENGTH; i++) referenceData[i] = i;

		double[] obtainedData = new double[EXPECTED_DATA_LENGTH];
		db.get(obtainedData);

		assertThat(obtainedData, is(equalTo(referenceData)));
	}
}
