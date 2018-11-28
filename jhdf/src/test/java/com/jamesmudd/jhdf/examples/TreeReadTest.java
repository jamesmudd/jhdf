package com.jamesmudd.jhdf.examples;

import org.junit.Test;

public class TreeReadTest {

	@Test
	public void testRunningTreeRead() throws Exception {
		TreeRead.main(new String[] { "src/test/resources/com/jamesmudd/jhdf/test_file.hdf5" });
	}

}
