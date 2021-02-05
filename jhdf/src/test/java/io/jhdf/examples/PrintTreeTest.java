/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PrintTreeTest {

	@Test
    void testOpeningV1File() {
		assertDoesNotThrow(() ->
			PrintTree.main(new String[] { "src/test/resources/hdf5/test_file.hdf5" }));
	}

	@Test
    void testOpeningV2File() {
		assertDoesNotThrow(() ->
			PrintTree.main(new String[] { "src/test/resources/hdf5/test_file2.hdf5" }));
	}

}
