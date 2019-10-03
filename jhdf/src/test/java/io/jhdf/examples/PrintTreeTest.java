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

import org.junit.jupiter.api.Test;

class PrintTreeTest {

	@Test
    void testOpeningV1File() {
		PrintTree.main(new String[] { "src/test/resources/io/jhdf/test_file.hdf5" });
	}

	@Test
    void testOpeningV2File() {
		PrintTree.main(new String[] { "src/test/resources/io/jhdf/test_file2.hdf5" });
	}

}
