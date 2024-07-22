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

import static org.apache.commons.lang3.StringUtils.removeStart;

public class NodeXml {

	@JacksonXmlProperty(localName = "OBJ-XID")
	String objId;

	@JacksonXmlProperty(localName = "H5Path")
	String path;

	@JacksonXmlProperty(localName = "Name")
	String name;

	public long getObjectId() {
		return Long.parseLong(removeStart(objId, "xid_"));
	}

}
