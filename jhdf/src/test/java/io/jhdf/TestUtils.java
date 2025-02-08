/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
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

	public static HdfFile loadTestHdfFile(String fileName) {
		Path testPath = getTestPath(fileName);
		return new HdfFile(testPath);
	}

	public static File getTestFile(String fileName) {
		Path testPath = getTestPath(fileName);
		return testPath.toFile();
	}

	public static Path getTestPath(String fileName) {
		URI testUri = getTestUri(fileName);
		return Paths.get(testUri);
	}

	public static URI getTestUri(String fileName) {
		URL url = TestUtils.class.getResource("/hdf5/" + fileName);
		if (url == null) {
			throw new RuntimeException("Could not find test file named: " + fileName);
		}
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Cannot convert URL \"" + url + "\" to URI: " + e, e);
		}
	}

	public static double[] toDoubleArray(Object data) {
		return Arrays.stream(Utils.flatten(data))
			.mapToDouble(el -> Double.parseDouble(el.toString()))
			.toArray();
	}

	public static String[] toStringArray(Object data) {
		return Arrays.stream(Utils.flatten(data))
			.map(el -> el.toString())
			.toArray(String[]::new);
	}

	public static Boolean[] toBooleanArray(Object data) {
		return Arrays.stream(Utils.flatten(data))
			.map(el -> parseBoolean(el.toString()))
			.toArray(Boolean[]::new);
	}

	private static Boolean parseBoolean(String str) {
		Boolean aBoolean = BooleanUtils.toBooleanObject(str);
		if(aBoolean != null) {
			return aBoolean;
		}
		// Used for parsing h5dump output
		return BooleanUtils.toBooleanObject(str, "0x01", "0x00", "null");
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
		assertThat(attribute1.getJavaType(), is(equalTo(attribute2.getJavaType())));
		assertThat(attribute1.isScalar(), is(equalTo(attribute2.isScalar())));
		assertThat(attribute1.isEmpty(), is(equalTo(attribute2.isEmpty())));


		if(attribute1.getJavaType() == String.class) {
			assertArrayEquals(toStringArray(attribute1.getData()), toStringArray(attribute2.getData()));
		} else if (attribute1.getJavaType() == boolean.class ||
					attribute1.getJavaType() == Boolean.class) {
			assertArrayEquals(toBooleanArray(attribute1.getData()), toBooleanArray(attribute2.getData()));
		} else {
			assertArrayEquals(toDoubleArray(attribute1.getData()), toDoubleArray(attribute2.getData()), 0.002);
		}
	}

	private static void compareDatasets(Dataset dataset1, Dataset dataset2) {
		logger.info("Comparing dataset2 [{}] on node [{}]", dataset1.getName(), dataset1.getPath());
		assertThat(dataset1.getName(), is(equalTo(dataset2.getName())));
		assertThat(dataset1.getDimensions(), is(equalTo(dataset2.getDimensions())));
		assertThat(dataset1.getJavaType(), is(equalTo(dataset2.getJavaType())));
		if(dataset1.getJavaType() == String.class) {
			assertArrayEquals(toStringArray(dataset1.getData()), toStringArray(dataset2.getData()));
		} else if (dataset1.getJavaType() == boolean.class ||
			dataset1.getJavaType() == Boolean.class) {
			assertArrayEquals(toBooleanArray(dataset1.getData()), toBooleanArray(dataset2.getData()));
		} else {
			assertArrayEquals(toDoubleArray(dataset1.getData()), toDoubleArray(dataset2.getData()), 0.002);
		}
	}
}
