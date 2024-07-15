/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.h5dump;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.jhdf.TestUtils.toDoubleArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class H5Dump {

	private static final Logger logger = LoggerFactory.getLogger(H5Dump.class);

	private static final XmlMapper XML_MAPPER;
	static {
		XML_MAPPER =  new XmlMapper();
		XML_MAPPER.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
		XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public static HDF5FileXml dumpAndParse(Path path) throws IOException, InterruptedException {
		logger.info("Reading [{}] with h5dump", path.toAbsolutePath());
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("h5dump", "--format=%.10lf", "--xml", path.toAbsolutePath().toString());
		processBuilder.redirectErrorStream(true); // get stderr as well
		logger.info("Starting h5dump process [{}]", processBuilder.command());
		Process process = processBuilder.start();
  		String xmlString = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
		process.waitFor(30, TimeUnit.SECONDS);
		logger.info("h5dump returned [{}] output [{}]", process.exitValue(), xmlString);
		// Validate
		assertThat(process.exitValue(), is(equalTo(0)));
		assertThat(xmlString, is(not(blankOrNullString())));
		// Parse the XML
        return XML_MAPPER.readValue(xmlString, HDF5FileXml.class);
	}

	public static void assetXmlAndHdfFileMatch(HDF5FileXml hdf5FileXml, HdfFile hdfFile) {
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
		for (AttributeXml attribute : groupXml.attributes) {
			compareAttributes(attribute, group.getAttribute(attribute.name));
		}
	}

	public static void assetHdfFilesMatch(WritableHdfFile hdfFile1, HdfFile hdfFile2) {
		// First validate the root group size
		compareGroups(hdfFile1, hdfFile2);
	}

	private static void compareAttributes(AttributeXml attributeXml, Attribute attribute) {
		logger.info("Comparing attribute [{}] on node [{}]", attribute.getName(), attribute.getNode().getPath());
		assertThat(attributeXml.name, is(equalTo(attribute.getName())));
		assertThat(attributeXml.getDimensions(), is(equalTo(attribute.getDimensions())));
		assertArrayEquals(toDoubleArray(attributeXml.getData()), toDoubleArray(attribute.getData()), 0.002);
	}

	private static void compareDatasets(DatasetXml datasetXml, Dataset dataset) {
		logger.info("Comparing dataset [{}] on node [{}]", dataset.getName(), dataset.getPath());
		assertThat(datasetXml.getObjectId(), is(equalTo(dataset.getAddress())));
		assertThat(datasetXml.getDimensions(), is(equalTo(dataset.getDimensions())));
		assertArrayEquals(toDoubleArray(datasetXml.getData()), toDoubleArray(dataset.getData()), 0.002);
	}


	public static void compareGroups(Group group1, Group group2) {
		// First validate the group size
		assertThat(group1.getChildren().size(), is(equalTo(group2.getChildren().size())));

		for (Map.Entry<String, Node> entry : group1.getChildren().entrySet()) {
			for (Map.Entry<String, Attribute> attributeEntry : entry.getValue().getAttributes().entrySet()) {
				Node group2Child = group2.getChild(entry.getValue().getName());
				compareAttributes(attributeEntry.getValue(), group2Child.getAttribute(attributeEntry.getKey()));
			}

			if(entry.getValue().isGroup()) {
					compareGroups((Group) entry.getValue(), (Group) group2.getChild(entry.getKey()));
				} else if (entry.getValue() instanceof Dataset) {
					compareDatasets((Dataset) entry.getValue(), (Dataset) group2.getChild(entry.getKey()));
				}
			}
	}

	private static void compareAttributes(Attribute attribute1, Attribute attribute2) {
		logger.info("Comparing attribute [{}] on node [{}]", attribute1.getName(), attribute1.getNode().getPath());
		assertThat(attribute1.getName(), is(equalTo(attribute2.getName())));
		assertThat(attribute1.getDimensions(), is(equalTo(attribute2.getDimensions())));
		assertArrayEquals(toDoubleArray(attribute1.getData()), toDoubleArray(attribute2.getData()), 0.002);
	}

	private static void compareDatasets(Dataset dataset1, Dataset dataset2) {
		logger.info("Comparing dataset2 [{}] on node [{}]", dataset1.getName(), dataset1.getPath());
		assertThat(dataset1.getDimensions(), is(equalTo(dataset2.getDimensions())));
		assertArrayEquals(toDoubleArray(dataset1.getData()), toDoubleArray(dataset2.getData()), 0.002);
	}

}
