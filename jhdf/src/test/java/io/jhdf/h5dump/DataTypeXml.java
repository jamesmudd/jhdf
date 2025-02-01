/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DataTypeXml {
	@JacksonXmlProperty(localName = "AtomicType")
	AtomicTypeXml atomicType;

	private static class AtomicTypeXml {

		@JacksonXmlProperty(localName = "IntegerType")
		IntegerTypeXml type;

	}

	private static class IntegerTypeXml {
		@JacksonXmlProperty(localName = "ByteOrder")
		String byteOrder;

		@JacksonXmlProperty(localName = "Sign")
		boolean signed;

		@JacksonXmlProperty(localName = "Size")
		int size;
	}
}
