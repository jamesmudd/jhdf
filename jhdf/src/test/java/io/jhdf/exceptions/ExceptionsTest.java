/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ExceptionsTest {

	private static final String TEST_MESSAGE = "test message";

	@Test
	void testHdfException() {
		HdfException ex = new HdfException(TEST_MESSAGE);
		assertThat(ex.getMessage(), is(TEST_MESSAGE));
		assertThat(ex.getCause(), is(nullValue()));
	}

	@Test
	void testHdfExceptionWithWrappedException() {
		IOException ioException = new IOException();
		HdfException ex = new HdfException(TEST_MESSAGE, ioException);
		assertThat(ex.getMessage(), is(TEST_MESSAGE));
		assertThat(ex.getCause(), is(sameInstance(ioException)));
	}

	@Test
	void testUnsupportedHdfException() {
		UnsupportedHdfException ex = new UnsupportedHdfException(TEST_MESSAGE);
		assertThat(ex.getMessage(), is(TEST_MESSAGE));
		assertThat(ex.getCause(), is(nullValue()));
	}

}
