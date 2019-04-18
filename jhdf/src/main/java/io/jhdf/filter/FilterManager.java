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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.object.message.FilterPipelineMessage;
import io.jhdf.object.message.FilterPipelineMessage.FilterInfo;

/**
 * This is a singleton for managing the loaded HDF5 filters.
 *
 * @author James Mudd
 */
public enum FilterManager {
	INSTANCE; // Enum singleton pattern

	private static final Logger logger = LoggerFactory.getLogger(FilterManager.class);

	private static final Map<Integer, Filter> ID_TO_FILTER = new HashMap<>();

	static {
		logger.info("Initalising HDF5 filters...");

		// Load the built in filters
		addFilter(new DeflatePipelineFilter());
		addFilter(new ByteShuffleFilter());

		// Add dynamically loaded filters
		ServiceLoader<Filter> serviceLoader = ServiceLoader.load(Filter.class);
		for (Filter pipelineFilter : serviceLoader) {
			addFilter(pipelineFilter);
		}

		logger.info("Initalized HDF5 filters");
	}

	/**
	 * Adds a filter. This can be used to add dynamically loaded filters. Validates
	 * the passed in filter to ensure in meets the specification, see
	 * {@link Filter}.
	 *
	 * @param filter the filter class to add
	 * @throws HdfFilterException if the filter is not valid
	 */
	public static void addFilter(Filter filter) {
		// Add the filter
		ID_TO_FILTER.put(filter.getId(), filter);

		logger.info("Added HDF5 filter '{}' with ID '{}'", filter.getName(), filter.getId());
	}

	/**
	 * Builds a new pipeline for decoding chunks from a
	 * {@link FilterPipelineMessage}.
	 *
	 * @param filterPipelineMessage message containing the datasets filter
	 *                              specification.
	 * @return the new pipeline
	 */
	public static FilterPipeline getPipeline(FilterPipelineMessage filterPipelineMessage) {
		List<FilterInfo> filters = filterPipelineMessage.getFilters();

		// Decoding so reverse order
		Collections.reverse(filters);

		// Make the new pipeline
		FilterPipeline pipeline = new FilterPipeline();
		// Add each filter
		filters.forEach(filter -> pipeline.addFilter(ID_TO_FILTER.get(filter.getId()), filter.getData()));

		return pipeline;
	}

}
