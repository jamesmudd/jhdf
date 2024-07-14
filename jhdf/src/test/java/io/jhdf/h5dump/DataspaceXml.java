/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class DataspaceXml {

	@JacksonXmlProperty(localName = "SimpleDataspace")
	SimpleDataspace simpleDataspace;

	public static class SimpleDataspace {
		@JacksonXmlProperty(localName = "Dimension")
		@JacksonXmlElementWrapper(useWrapping = false)
		List<DimensionXml> dimensions;
	}

	public static class DimensionXml {
		  @JacksonXmlProperty(localName = "DimSize")
		int size;
		  @JacksonXmlProperty(localName = "MaxDimSize")
		int maxSize;
	}

	public int[] getDimensions() {
		if(simpleDataspace == null) {
			// Its scalar
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			return simpleDataspace.dimensions.stream()
				.mapToInt(dim -> dim.size)
				.toArray();
		}
	}
}
