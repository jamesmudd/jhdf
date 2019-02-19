package io.jhdf.examples;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class PrintTreeTest {

	@Test
	public void testOpeningV1File() throws IOException {
		PrintTree.main(new String[] { "src/test/resources/io/jhdf/test_file.hdf5" });
	}

	@Test
	public void testOpeningV2File() throws IOException {
		PrintTree.main(new String[] { "src/test/resources/io/jhdf/test_file2.hdf5" });
	}

}
