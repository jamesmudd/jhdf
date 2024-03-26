package h5dump;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jhdf.api.Group;

public class HDF5FileXml {

	@JsonProperty("RootGroup")
	GroupXml rootGroup;
}
