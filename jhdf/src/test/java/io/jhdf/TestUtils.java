/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public final class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
	private static final String ALPHANUMERIC_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private TestUtils() {
		throw new AssertionError("No instances of TestUtils");
	}

	/**
	 * Generates a random alphanumeric string with a random length in the given range.
	 *
	 * @param minLengthInclusive the minimum length of the generated string (inclusive)
	 * @param maxLengthExclusive the maximum length of the generated string (exclusive)
	 * @return a random alphanumeric string
	 */
	public static String randomAlphanumeric(int minLengthInclusive, int maxLengthExclusive) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int length = random.nextInt(minLengthInclusive, maxLengthExclusive);
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
		}
		return sb.toString();
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

	public static Integer[] toObject(int[] array) {
		Integer[] result = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static Byte[] toObject(byte[] array) {
		Byte[] result = new Byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static Float[] toObject(float[] array) {
		Float[] result = new Float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static Double[] toObject(double[] array) {
		Double[] result = new Double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static int[] subarray(int[] array, int startIndexInclusive, int endIndexExclusive) {
		return Arrays.copyOfRange(array, startIndexInclusive, endIndexExclusive);
	}

	public static float[] subarray(float[] array, int startIndexInclusive, int endIndexExclusive) {
		return Arrays.copyOfRange(array, startIndexInclusive, endIndexExclusive);
	}

	public static double[] subarray(double[] array, int startIndexInclusive, int endIndexExclusive) {
		return Arrays.copyOfRange(array, startIndexInclusive, endIndexExclusive);
	}

	public static Boolean[] toBooleanArray(Object data) {
		return Arrays.stream(Utils.flatten(data))
			.map(el -> parseBoolean(el.toString()))
			.toArray(Boolean[]::new);
	}

	private static Boolean parseBoolean(String str) {
		if (str == null) {
			return null;
		}
		switch (str.toLowerCase(Locale.ROOT)) {
			case "true": case "yes": case "y": case "on": case "1":
				return Boolean.TRUE;
			case "false": case "no": case "n": case "off": case "0":
				return Boolean.FALSE;
			default:
				break;
		}
		// Used for parsing h5dump output
		switch (str) {
			case "0x01":
				return Boolean.TRUE;
			case "0x00":
				return Boolean.FALSE;
			case "null":
				return null;
			default:
				throw new IllegalArgumentException("The String did not match any specified value: " + str);
		}
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
