/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import java.util.Arrays;

public class PipelineFilterWithData {

	private final Filter filter;
	private final int[] filterData;

	PipelineFilterWithData(Filter filter, int[] filterData) {
		this.filter = filter;
		this.filterData = filterData;
	}

	byte[] decode(byte[] data) {
		return filter.decode(data, filterData);
	}

	@Override
	public String toString() {
		return filter.getName() + " (id=" + filter.getId() + ") data=" + Arrays.toString(filterData);
	}

	public String getName() {
		return filter.getName();
	}

	public int getId() {
		return filter.getId();
	}

	public int[] getFilterData() {
		return filterData.clone();
	}
}
