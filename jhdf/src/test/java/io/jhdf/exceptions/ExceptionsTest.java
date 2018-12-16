package io.jhdf.exceptions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

public class ExceptionsTest {

	@Test
	public void testHdfException() throws Exception {
		HdfException ex = new HdfException("test message");
		assertThat(ex.getMessage(), is("test message"));
		assertThat(ex.getCause(), is(nullValue()));
	}

	@Test
	public void testHdfExceptionWithWrappedException() throws Exception {
		IOException ioException = new IOException();
		HdfException ex = new HdfException("test message", ioException);
		assertThat(ex.getMessage(), is("test message"));
		assertThat(ex.getCause(), is(sameInstance(ioException)));
	}

	@Test
	public void testUnsupportedHdfException() throws Exception {
		UnsupportedHdfException ex = new UnsupportedHdfException("test message");
		assertThat(ex.getMessage(), is("test message"));
		assertThat(ex.getCause(), is(nullValue()));
	}

}
