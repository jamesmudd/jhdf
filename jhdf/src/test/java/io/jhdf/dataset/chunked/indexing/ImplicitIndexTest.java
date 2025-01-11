/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ImplicitIndexTest {

	private static final String HDF5_TEST_FILE_NAME = "implicit_index_datasets.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		hdfFile.close();
	}

	@Test
	void testDataReadCorrectly() {
		// Unfiltered
		Dataset implicitIndex = hdfFile.getDatasetByPath("implicit_index_exact");
		int[] implicitIndexDataFlat = (int[]) implicitIndex.getDataFlat();
		assertThat(implicitIndexDataFlat).isEqualTo(expectedData(Math.toIntExact(implicitIndex.getSize())));

		Dataset indexMismatch = hdfFile.getDatasetByPath("implicit_index_mismatch");
		int[] indexMismatchDataFlat = (int[]) indexMismatch.getDataFlat();
		assertThat(indexMismatchDataFlat).isEqualTo(expectedData(Math.toIntExact(indexMismatch.getSize())));

	}

	private int[] expectedData(int length) {
		int[] data = new int[length];
		for (int i = 0; i < length; i++) {
			data[i] = i;
		}
		return data;
	}
}
