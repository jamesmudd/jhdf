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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang3.StringUtils;

public class DatasetXml extends NodeXml {

	@JacksonXmlProperty(localName = "StorageLayout")
	String storageLayout;
	@JacksonXmlProperty(localName = "FillValueInfo")
	String fillValue;

	@JacksonXmlProperty(localName = "Data")
	DataXml data;

	@JacksonXmlProperty(localName = "Dataspace")
	DataspaceXml dataspace;

	@JacksonXmlProperty(localName = "DataType")
	DataTypeXml dataType;

	public static class DataXml {
		@JacksonXmlProperty(localName = "DataFromFile")
		String dataString;
	}

	public String[] getData() {
		return StringUtils.split(data.dataString);
	}

	public int[] getDimensions() {
		return dataspace.simpleDataspace.dimensions.stream()
			.mapToInt(dim -> dim.size)
			.toArray();
	}

	public static class DataTypeXml {
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
			boolean sign;

			@JacksonXmlProperty(localName = "Size")
			int size;
		}
	}
}
