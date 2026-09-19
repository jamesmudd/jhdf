/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;

import java.util.ArrayList;
import java.util.BitSet;
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
	private static final BitSet NO_FILTERS_SKIPPED = new BitSet();

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
		return decode(encodedData, NO_FILTERS_SKIPPED);
	}

	/**
	 * Applies the filters in this pipeline to decode the data, skipping filters that
	 * were not applied to the chunk when it was encoded.
	 *
	 * @param encodedData the data to be decoded
	 * @param filterMask  the per-chunk filter mask; a set bit skips the filter at
	 *                    the corresponding pipeline position
	 * @return the decoded data
	 * @throws HdfFilterException if the decode operation fails
	 */
	public byte[] decode(byte[] encodedData, BitSet filterMask) {

		// Apply the filters, decoding so reverse order
		for (int i = filters.size() - 1; i >= 0; i--) {
			if (!filterMask.get(i)) {
				PipelineFilterWithData filter = filters.get(i);
				encodedData = filter.decode(encodedData);
			}
		}

		return encodedData;
	}

	/**
	 * Applies all the filters in this pipeline to encode the data for writing.
	 *
	 * @param data the data to be encoded
	 * @return the encoded data
	 * @throws HdfFilterException if the encode operation fails
	 */
	public byte[] encode(byte[] data) {

		// Apply the filters, encoding so pipeline (forward) order
		for (PipelineFilterWithData filter : filters) {
			data = filter.encode(data);
		}

		return data;
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
