/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class PrintTreeTest {

	@Test
	public void testOpeningV1File() {
		PrintTree.main(new String[] { "src/test/resources/io/jhdf/test_file.hdf5" });
	}

	@Test
	public void testOpeningV2File() {
		PrintTree.main(new String[] { "src/test/resources/io/jhdf/test_file2.hdf5" });
	}

}
