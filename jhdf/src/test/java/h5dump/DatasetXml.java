package h5dump;

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
}
