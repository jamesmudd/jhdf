package h5dump;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.jhdf.HdfFile;
import io.jhdf.TestUtils;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class H5Dump {

	private static final XmlMapper xmlMapper;
	static {
		xmlMapper =  new XmlMapper();
		xmlMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
		xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public static HDF5FileXml dumpAndParse(Path path) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("h5dump", "--format=%.1lf", "--xml", path.toAbsolutePath().toString());
		Process start = processBuilder.start();
		String xmlString = IOUtils.toString(start.getInputStream(), StandardCharsets.UTF_8);
        return xmlMapper.readValue(xmlString, HDF5FileXml.class);
	}

	public static void compareXmlToFile(HDF5FileXml hdf5FileXml, HdfFile hdfFile) {
		// First validate the root group size
		compareGroups(hdf5FileXml.rootGroup, hdfFile);
	}

	public static void compareGroups(GroupXml groupXml, Group group) {
		// First validate the group size
		assertThat(groupXml.children(), is(equalTo(group.getChildren().size())));
		assertThat(groupXml.getObjectId(), is(equalTo(group.getAddress())));
		for (GroupXml childGroup : groupXml.groups) {
			compareGroups(childGroup, (Group) group.getChild(childGroup.name));
		}
		for (DatasetXml dataset : groupXml.datasets) {
			compareDatasets(dataset, (Dataset) group.getChild(dataset.name));
		}
	}

	private static void compareDatasets(DatasetXml datasetXml, Dataset dataset) {
		assertThat(datasetXml.getObjectId(), is(equalTo(dataset.getAddress())));
		assertThat(datasetXml.getDimensions(), is(equalTo(dataset.getDimensions())));
		assertThat(datasetXml.getData(), is(equalTo(TestUtils.toStringArray(dataset.getData()))));
	}

}
