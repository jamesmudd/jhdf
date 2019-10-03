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

class ReadDatasetTest {

	@Test
    void testOpeningV1File() {
		ReadDataset.main(new String[] { "src/test/resources/io/jhdf/test_file.hdf5", "/nD_Datasets/3D_float32" });
	}

	@Test
    void testOpeningV2File() {
		ReadDataset.main(new String[] { "src/test/resources/io/jhdf/test_file2.hdf5", "/nD_Datasets/3D_float32" });
	}

}
