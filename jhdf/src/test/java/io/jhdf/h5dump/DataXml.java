package io.jhdf.h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DataXml {
	@JacksonXmlProperty(localName = "DataFromFile")
	String dataString;
}
