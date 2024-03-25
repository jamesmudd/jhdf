package h5dump;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class DataspaceXml {

	@JacksonXmlProperty(localName = "SimpleDataspace")
		SimpleDataspace simpleDataspace;

	public static class SimpleDataspace {
		@JacksonXmlProperty(localName = "Dimension")
		@JacksonXmlElementWrapper(useWrapping = false)
		List<DimensionXml> dimensions;
	}

	public static class DimensionXml {
		  @JacksonXmlProperty(localName = "DimSize")
		int size;
		  @JacksonXmlProperty(localName = "MaxDimSize")
		int maxSize;
	}
}
