/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public final class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

	private TestUtils() {
		throw new AssertionError("No instances of TestUtils");
	}

	public static HdfFile loadTestHdfFile(String fileName) throws Exception {
		URL url = TestUtils.class.getResource("/hdf5/" + fileName);
		if (url == null) {
			throw new RuntimeException("Could not find test file named: " + fileName);
		}
		return new HdfFile(Paths.get(url.toURI()));
	}

	public static double[] toDoubleArray(Object data) {
		return Arrays.stream(Utils.flatten(data))
			.mapToDouble(el -> Double.parseDouble(el.toString()))
			.toArray();
	}

	public static void compareGroups(Group group1, Group group2) {
		logger.info("Comparing groups [{}]", group1.getPath());

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
