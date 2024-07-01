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
		boolean sign;

		@JacksonXmlProperty(localName = "Size")
		int size;
	}
}
