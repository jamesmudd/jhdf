package h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class DatasetXml extends NodeXml {

	@JacksonXmlProperty(localName = "StorageLayout")
	String storageLayout;
	@JacksonXmlProperty(localName = "FillValueInfo")
	String fillValue;

	@JacksonXmlProperty(localName = "Data")
	String data;

	@JacksonXmlProperty(localName = "Dataspace")
	DataspaceXml dataspace;
}
