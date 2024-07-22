/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class HDF5FileXml {

	@JacksonXmlProperty(localName = "RootGroup")
	GroupXml rootGroup;
}
