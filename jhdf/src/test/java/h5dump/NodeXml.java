package h5dump;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeXml {

	@JsonProperty("OBJ-XID")
	String objId;

	@JsonProperty("H5Path")
	String path;

}
