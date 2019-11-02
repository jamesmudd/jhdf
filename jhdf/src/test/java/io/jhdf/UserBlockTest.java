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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class UserBlockTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_userblock_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_userblock_latest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() throws Exception {
		earliestHdfFile = loadTestHdfFile(HDF5_TEST_EARLIEST_FILE_NAME);
		latestHdfFile = loadTestHdfFile(HDF5_TEST_LATEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		earliestHdfFile.close();
		latestHdfFile.close();
	}

	@Test
	void testEarliest() {
		assertThat(earliestHdfFile.getUserBlockSize(), is(equalTo(512L)));

		ByteBuffer userBlockBuffer = earliestHdfFile.getUserBlockBuffer();
		assertThat(userBlockBuffer, is(notNullValue()));

		// Now read the data and check it
		String data = UTF_8.decode(userBlockBuffer).toString().trim();
		assertThat(data, is(equalTo("userblock data here...")));
	}

	@Test
	void testLatest() {
		assertThat(latestHdfFile.getUserBlockSize(), is(equalTo(1024L)));

		ByteBuffer userBlockBuffer = earliestHdfFile.getUserBlockBuffer();
		assertThat(userBlockBuffer, is(notNullValue()));

		// Now read the data and check it
		String data = UTF_8.decode(userBlockBuffer).toString().trim();
		assertThat(data, is(equalTo("userblock data here...")));
	}
}
