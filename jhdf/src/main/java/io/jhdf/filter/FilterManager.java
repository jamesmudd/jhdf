package io.jhdf.filter;

import java.util.HashMap;
import java.util.Map;

public final class FilterManager {

	private static final Map<Integer, PipelineFilter> idToFilter = new HashMap<>();

	static {
		idToFilter.put(1, new GzipPipelineFilter());
	}

	public static PipelineFilter getFilterById(int id) {
		return idToFilter.get(id);
	}

}
