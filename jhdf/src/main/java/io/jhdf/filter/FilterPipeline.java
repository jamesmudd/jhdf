/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of filters making up a ordered pipeline to decode chunks.
 *
 * @author James Mudd
 */
public class FilterPipeline {

	private static class PipelineFilterWithData {

		private final Filter filter;
		private final int[] filterData;

		private PipelineFilterWithData(Filter filter, int[] filterData) {
			this.filter = filter;
			this.filterData = filterData;
		}

		private byte[] decode(byte[] data) {
			return filter.decode(data, filterData);
		}
	}

	private final List<PipelineFilterWithData> filters = new ArrayList<>();

	/* package */ FilterPipeline() {
	}

	/* package */ void addFilter(Filter filter, int[] data) {
		filters.add(new PipelineFilterWithData(filter, data));
	}

	/**
	 * Applies all the filters in this pipeline to decode the data.
	 *
	 * @param encodedData the data to be decoded
	 * @return the decoded data
	 * @throws HdfFilterException if the decode operation fails
	 */
	public byte[] decode(byte[] encodedData) {

		// Apply the filters
		for (PipelineFilterWithData b : filters) {
			encodedData = b.decode(encodedData);
		}

		return encodedData;
	}

}
