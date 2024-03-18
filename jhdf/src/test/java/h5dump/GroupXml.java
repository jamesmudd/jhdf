package h5dump;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class GroupXml extends NodeXml {

	@JacksonXmlProperty(localName = "Group")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<GroupXml> groups;

	@JacksonXmlProperty(localName = "Dataset")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<DatasetXml> datasets;

}
