/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

public class OpaqueDatasetTest {

	private static final String HDF5_TEST_EARLIEST_FILE_NAME = "opaque_datasets_earliest.hdf5";
	private static final String HDF5_TEST_LATEST_FILE_NAME = "opaque_datasets_latest.hdf5";

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

	private String toTimeStampString(byte[] bytes) {
		return Instant.ofEpochSecond(ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN).getLong()).toString();
	}

	@Test
	void testTimestampsEarliest() {
		Dataset dataset = earliestHdfFile.getDatasetByPath("opaque");
		assertThat(dataset.getJavaType(), is(byte[].class));
		assertThat(dataset.getDimensions(), is(new int[]{5}));
		byte[][] data = (byte[][]) dataset.getData();
		// Check timestamps match numpy datetime64
		assertThat(toTimeStampString(data[0]), is("2017-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[1]), is("2018-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[2]), is("2019-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[3]), is("2020-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[4]), is("2021-02-22T14:14:14Z"));
	}

	@Test
	void testTimestampsLatest() {
		Dataset dataset = latestHdfFile.getDatasetByPath("opaque");
		assertThat(dataset.getJavaType(), is(byte[].class));
		assertThat(dataset.getDimensions(), is(new int[]{5}));
		byte[][] data = (byte[][]) dataset.getData();
		// Check timestamps match numpy datetime64
		assertThat(toTimeStampString(data[0]), is("2017-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[1]), is("2018-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[2]), is("2019-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[3]), is("2020-02-22T14:14:14Z"));
		assertThat(toTimeStampString(data[4]), is("2021-02-22T14:14:14Z"));
	}
}
