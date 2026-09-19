/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.Test;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FilterMaskTest {

	@Test
	void readsRawChunkWhenConfiguredFilterWasSkipped() {
		try (HdfFile hdfFile = loadTestHdfFile("filter_mask.hdf5")) {
			Dataset dataset = hdfFile.getDatasetByPath("/raw_chunk");

			assertThat(dataset.getFilters(), hasSize(1));
			assertThat(dataset.getFilters().get(0).getName(), is("deflate"));
			assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, (byte[]) dataset.getData());
		}
	}
}
