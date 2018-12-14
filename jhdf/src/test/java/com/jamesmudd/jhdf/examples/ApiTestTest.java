package com.jamesmudd.jhdf.examples;

import java.io.IOException;

import org.junit.Test;

public class ApiTestTest {

	@Test
	public void testOpeningV1File() throws IOException {
		ApiTest.main(new String[] { "src/test/resources/com/jamesmudd/jhdf/test_file.hdf5" });
	}

	@Test
	public void testOpeningV2File() throws IOException {
		ApiTest.main(new String[] { "src/test/resources/com/jamesmudd/jhdf/test_file2.hdf5" });
	}

}
