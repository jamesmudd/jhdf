package h5dump;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.removeStart;

public class NodeXml {

	@JsonProperty("OBJ-XID")
	String objId;

	@JsonProperty("H5Path")
	String path;

	@JsonProperty("Name")
	String name;

	public long getObjectId() {
		return Long.parseLong(removeStart(objId, "xid_"));
	}

}
