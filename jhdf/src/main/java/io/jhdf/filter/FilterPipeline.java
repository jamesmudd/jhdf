/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

		@Override
		public String toString() {
			return "{" +
				"filter=" + filter.getName() +
				", filterData=" + Arrays.toString(filterData) +
				'}';
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

	@Override
	public String toString() {
		return "FilterPipeline{" +
			filters.stream().map(Objects::toString).collect(Collectors.joining(" -> ")) +
			'}';
	}
}
