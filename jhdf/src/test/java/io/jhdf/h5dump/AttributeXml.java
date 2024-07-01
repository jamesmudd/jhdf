package io.jhdf.h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class AttributeXml {

	@JacksonXmlProperty(localName = "Name")
	String name;

	@JacksonXmlProperty(localName = "Data")
	DataXml data;

	@JacksonXmlProperty(localName = "Dataspace")
	DataspaceXml dataspace;

	@JacksonXmlProperty(localName = "DataType")
	DataTypeXml dataType;

}
