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

class FixedArrayIndexTest {

	private static final String HDF5_TEST_FILE_NAME = "fixed_array_paged_datasets.hdf5";

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
		Dataset int8Unpaged = hdfFile.getDatasetByPath("fixed_array/int16_unpaged");
		short[] int8UnpagedData = (short[]) int8Unpaged.getDataFlat();
		assertThat(int8UnpagedData).isEqualTo(expectedData(Math.toIntExact(int8Unpaged.getSize())));

		Dataset int8TwoPage = hdfFile.getDatasetByPath("fixed_array/int16_two_page");
		short[] int8TwoPageData = (short[]) int8TwoPage.getDataFlat();
		assertThat(int8TwoPageData).isEqualTo(expectedData(Math.toIntExact(int8TwoPage.getSize())));

		Dataset int8FivePage = hdfFile.getDatasetByPath("fixed_array/int16_five_page");
		short[] int8FivePageData = (short[]) int8FivePage.getDataFlat();
		assertThat(int8FivePageData).isEqualTo(expectedData(Math.toIntExact(int8FivePage.getSize())));

		// Filtered
		Dataset int8UnpagedFiltered = hdfFile.getDatasetByPath("filtered_fixed_array/int16_unpaged");
		short[] int8UnpagedDataFiltered = (short[]) int8UnpagedFiltered.getDataFlat();
		assertThat(int8UnpagedDataFiltered).isEqualTo(expectedData(Math.toIntExact(int8UnpagedFiltered.getSize())));

		Dataset int8TwoPageFiltered = hdfFile.getDatasetByPath("filtered_fixed_array/int16_two_page");
		short[] int8TwoPageDataFiltered = (short[]) int8TwoPageFiltered.getDataFlat();
		assertThat(int8TwoPageDataFiltered).isEqualTo(expectedData(Math.toIntExact(int8TwoPageFiltered.getSize())));

		Dataset int8FivePageFiltered = hdfFile.getDatasetByPath("filtered_fixed_array/int16_five_page");
		short[] int8FivePageDataFiltered = (short[]) int8FivePageFiltered.getDataFlat();
		assertThat(int8FivePageDataFiltered).isEqualTo(expectedData(Math.toIntExact(int8FivePageFiltered.getSize())));
	}

	private short[] expectedData(int length) {
		short[] data = new short[length];
		for (short i = 0; i < length; i++) {
			data[i] = i;
		}
		return data;
	}
}
