package h5dump;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Collections;
import java.util.List;

public class GroupXml extends NodeXml {

	@JacksonXmlProperty(localName = "Group")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<GroupXml> groups = Collections.emptyList();

	@JacksonXmlProperty(localName = "Dataset")
	@JacksonXmlElementWrapper(useWrapping = false)
	List<DatasetXml> datasets = Collections.emptyList();

	public int children() {
		return groups.size() + datasets.size();
	}

}
