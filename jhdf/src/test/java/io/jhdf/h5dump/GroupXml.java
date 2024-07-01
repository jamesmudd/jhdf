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

import java.util.Collections;
import java.util.List;

public class GroupXml extends NodeXml {

	@JacksonXmlProperty(localName = "Group")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<GroupXml> groups = Collections.emptyList();

	@JacksonXmlProperty(localName = "Dataset")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<DatasetXml> datasets = Collections.emptyList();
	@JacksonXmlProperty(localName = "Attribute")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<AttributeXml> attributes = Collections.emptyList();

	public int children() {
		return groups.size() + datasets.size();
	}

}
