/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 * 
 * http://jhdf.io
 * 
 * Copyright 2019 James Mudd
 * 
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.object.message.FilterPipelineMessage;
import io.jhdf.object.message.FilterPipelineMessage.Filter;

public final class FilterManager {

	private static final Map<Integer, Class<? extends PipelineFilter>> idToFilter = new HashMap<>();

	static {
		idToFilter.put(1, DeflatePipelineFilter.class);
		idToFilter.put(2, ByteShuffleFilter.class);
	}

	private FilterManager() {
		// No Instances
	}

	private static PipelineFilter getNewFilterInstance(int id, int[] data) {
		try {
			// Must have a constructor taking int[] for the filter data
			return idToFilter.get(id).getConstructor(int[].class).newInstance(data);
		} catch (Exception e) {
			throw new HdfFilterException("Could not instanciate filter for id=" + id, e);
		}
	}

	public static FilterPipeline getPipeline(FilterPipelineMessage filterPipelineMessage) {
		List<Filter> filters = filterPipelineMessage.getFilters();

		// Decoding so reverse order
		Collections.reverse(filters);

		List<PipelineFilter> pipelineFilters = new ArrayList<>();
		for (Filter filter : filters) {
			PipelineFilter pipelineFilter = getNewFilterInstance(filter.getId(), filter.getData());
			pipelineFilters.add(pipelineFilter);
		}

		return new FilterPipeline(pipelineFilters);
	}

}
