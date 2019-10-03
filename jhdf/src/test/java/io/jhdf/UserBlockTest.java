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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class UserBlockTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "test_userblock_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "test_userblock_latest.hdf5";

	private static HdfFile earliestHdfFile;
	private static HdfFile latestHdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = UserBlockTest.class.getResource(HDF5_TEST_EARLIEST_FILE_NAME).getFile();
		earliestHdfFile = new HdfFile(new File(earliestTestFileUrl));
		String latestTestFileUrl = UserBlockTest.class.getResource(HDF5_TEST_LATEST_FILE_NAME).getFile();
		latestHdfFile = new HdfFile(new File(latestTestFileUrl));
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
