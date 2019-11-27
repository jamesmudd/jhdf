/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.getDimensions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * This is a "catch all" test designed to look at all the test HDF5 files and
 * fully explore the tree in all of them. In doing so it exercises most of the
 * jHDF code and validates the behaviour of groups and datasets.
 *
 * @author James Mudd
 */
class TestAllFiles {

	private static final PathMatcher HDF5 = FileSystems.getDefault().getPathMatcher("glob:**.{hdf5,h5}");

	@TestFactory
	Stream<DynamicNode> allHdf5TestFiles() throws IOException, URISyntaxException {

		// Auto discover the test files assuming they exist in under the directory
		// containing test_file.hdf5
		URL resource = this.getClass().getResource("/hdf5/");
		Path path = Paths.get(resource.toURI()).getParent();
		List<Path> files = Files.walk(path).filter(HDF5::matches).collect(Collectors.toList());

		// Check at least some files have been discovered
		assertThat("Less than 3 HDF5 test files discovered searched paths below: " + path.toAbsolutePath(),
				files.size(), is(greaterThan(2)));

		// Make a test for each file
		return files.stream().map(this::createTest);
	}

	private DynamicNode createTest(Path path) {
		return dynamicTest(path.getFileName().toString(), () -> {
			try (HdfFile hdfFile = new HdfFile(path.toFile())) {
				verifyAttributes(hdfFile);
				recurseGroup(hdfFile);
			}
		});
	}

	private void recurseGroup(Group group) {
		for (Node node : group) {
			if (node instanceof Group) {
				Group group2 = (Group) node;
				verifyGroup(group2);
				recurseGroup(group2);
			} else if (node instanceof Dataset) {
				verifyDataset((Dataset) node, group);
			}
			verifyAttributes(node);
		}
	}

	private void verifyAttributes(Node node) {
		if(node instanceof Link) {
			if(((Link) node).isBrokenLink()) {
				return; // Can't verify broken links
			}
		}
		for (Entry<String, Attribute> entry : node.getAttributes().entrySet()) {
			Attribute attribute = entry.getValue();
			assertThat(attribute.getName(), is(equalTo(entry.getKey())));
			assertThat(attribute.getJavaType(), is(notNullValue()));
			if (attribute.isEmpty()) {
				assertThat(attribute.getSize(), is(equalTo(0L)));
				assertThat(attribute.getDiskSize(), is(equalTo(0L)));
				assertThat(attribute.getData(), is(nullValue()));
			} else if (attribute.isScalar()) {
				assertThat(attribute.getSize(), is(equalTo(1L)));
				assertThat(attribute.getDiskSize(), is(greaterThan(0L)));
				assertThat(attribute.getData(), is(notNullValue()));
			} else {
				assertThat(attribute.getSize(), is(greaterThan(0L)));
				assertThat(attribute.getDiskSize(), is(greaterThan(0L)));
				assertThat(attribute.getData(), is(notNullValue()));
			}
		}
	}

	/**
	 * Verifies things that should be true about all datasets
	 *
	 * @param dataset the dataset to be exercised
	 * @param group   its parent group
	 */
	@SuppressWarnings("unchecked")
	private void verifyDataset(Dataset dataset, Group group) {
		assertThat(dataset.getName(), is(notNullValue()));
		assertThat(dataset.getPath(), is(group.getPath() + dataset.getName()));
		assertThat(dataset.getParent(), is(sameInstance(group)));
		int[] dims = dataset.getDimensions();
		assertThat(dims, is(notNullValue()));

		// Call getAttributes twice to check lazy initialisation
		assertThat(dataset.getAttributes(), is(notNullValue()));
		assertThat(dataset.getAttributes(), is(notNullValue()));

		assertThat(dataset.isGroup(), is(false));
		assertThat(dataset.isLink(), is(false));
		assertThat(dataset.getType(), is(NodeType.DATASET));
		assertThat(dataset.getDataLayout(), is(notNullValue()));

		// Call getData twice to check cases of lazy initialisation are working correctly
		dataset.getData();
		final Object data = dataset.getData();

		if (dataset.isEmpty()) {
			assertThat(data, is(nullValue()));
			// Empty so should have 0 size
			assertThat(dataset.getDiskSize(), is(equalTo(0L)));
		} else if (dataset.isScalar()) {
			assertThat(data.getClass(), is(equalTo(dataset.getJavaType())));
			// Should have some size
			assertThat(dataset.getDiskSize(), is(greaterThan(0L)));
		} else if (dataset.isCompound()) {
			// Compound datasets are currently returned as maps, maybe a custom CompoundDataset might be better in the future..
			assertThat(data, is(instanceOf(Map.class)));
			assertThat((Map<String, Object>) data, is(not(anEmptyMap())));
		} else {
			assertThat(getDimensions(data), is(equalTo(dims)));
			assertThat(getType(data), is(equalTo(dataset.getJavaType())));
			// Should have some size
			assertThat(dataset.getDiskSize(), is(greaterThan(0L)));
		}
	}

	private Class<?> getType(Object data) {
		if (Array.get(data, 0).getClass().isArray()) {
			return getType(Array.get(data, 0));
		} else {
			return data.getClass().getComponentType();
		}
	}

	/**
	 * Verifies things that should be true about all groups
	 *
	 * @param group to exercise
	 */
	private void verifyGroup(Group group) {
		assertThat(group.getAttributes(), is(notNullValue()));
		assertThat(group.isGroup(), is(true));
		assertThat(group.isLink(), is(false));
		assertThat(group.getType(), is(NodeType.GROUP));
	}
}
