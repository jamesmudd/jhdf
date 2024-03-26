package h5dump;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class NodeXml {

	@JsonProperty("OBJ-XID")
	String objId;

	@JsonProperty("H5Path")
	String path;

	@JsonProperty("Name")
	String name;

	int getObjectId() {
		return Integer.parseInt(StringUtils.removeStart(objId, "xid_"));
	}

}
