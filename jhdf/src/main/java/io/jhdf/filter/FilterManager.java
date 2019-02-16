package io.jhdf.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.FilterPipelineMessage;
import io.jhdf.object.message.FilterPipelineMessage.Filter;

public final class FilterManager {

	private static final Map<Integer, PipelineFilter> idToFilter = new HashMap<>();

	static {
		idToFilter.put(1, new GzipPipelineFilter());
	}

	private FilterManager() {
		// No Instances
	}

	public static PipelineFilter getFilterById(int id) {
		return idToFilter.get(id);
	}

	public static InputStream getPipeline(FilterPipelineMessage filterPipelineMessage, InputStream inputStream) {
		List<Filter> filters = filterPipelineMessage.getFilters();
		for (Filter filter : filters) {
			PipelineFilter pipelineFilter = idToFilter.get(filter.getId());
			try {
				inputStream = pipelineFilter.getPipelineFilter(inputStream);
			} catch (IOException e) {
				throw new HdfException("Error creating filter pipeline for filter: " + filter, e);
			}
		}
		return inputStream;
	}

}
