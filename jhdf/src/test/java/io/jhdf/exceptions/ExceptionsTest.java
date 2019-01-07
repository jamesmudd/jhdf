package io.jhdf.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.IOException;

import org.junit.jupiter.api.Test;

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
