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

class ReadDatasetTest {

	@Test
    void testOpeningV1File() {
		assertDoesNotThrow(() ->
			ReadDataset.main(new String[] { "src/test/resources/hdf5/test_file.hdf5", "/nD_Datasets/3D_float32" }));
	}

	@Test
    void testOpeningV2File() {
		assertDoesNotThrow(() ->
			ReadDataset.main(new String[] { "src/test/resources/hdf5/test_file2.hdf5", "/nD_Datasets/3D_float32" }));
	}

}
