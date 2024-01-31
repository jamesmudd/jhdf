/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A collection of filters making up a ordered pipeline to decode chunks.
 *
 * @author James Mudd
 */
public class FilterPipeline {

	public static final FilterPipeline NO_FILTERS = new FilterPipeline();

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

		// Apply the filters, decoding so reverse order
		for (int i = filters.size() -1; i >= 0; i--) {
			PipelineFilterWithData filter = filters.get(i);
			encodedData = filter.decode(encodedData);
		}

		return encodedData;
	}

	@Override
	public String toString() {
		return "FilterPipeline{" +
			filters.stream().map(Objects::toString).collect(Collectors.joining(" -> ")) +
			'}';
	}

	public List<PipelineFilterWithData> getFilters() {
		return Collections.unmodifiableList(filters);
	}
}
