package com.jamesmudd.jhdf.examples;

import java.io.IOException;

import org.junit.Test;

public class ApiTestTest {

	@Test
	public void testRunningApiTest() throws IOException {
		ApiTest.main(new String[] { "src/test/resources/com/jamesmudd/jhdf/test_file.hdf5" });
	}

}
