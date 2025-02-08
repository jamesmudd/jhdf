/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.TestUtils;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.dataset.ChunkedDataset;
import io.jhdf.api.dataset.ContiguousDataset;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jhdf.Utils.flatten;
import static io.jhdf.Utils.getDimensions;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * This is a "catch all" test designed to look at all the test HDF5 files and
 * fully explore the tree in all of them. In doing so it exercises most of the
 * jHDF code and validates the behaviour of groups and datasets.
 *
 * @author James Mudd
 */
public abstract class TestAllFilesBase {

	private static final PathMatcher HDF5 = FileSystems.getDefault().getPathMatcher("glob:**.{hdf5,h5}");

	@TestFactory
	Stream<DynamicNode> allHdf5TestFiles() throws IOException {

		// Auto discover the test files assuming they exist in under the directory
		// containing test_file.hdf5
		Path hdf5Path = TestUtils.getTestPath("");
		List<Path> files = Files.walk(hdf5Path).filter(HDF5::matches).collect(Collectors.toList());

		// Check at least some files have been discovered
		assertThat("Less than 3 HDF5 test files discovered searched paths below: " + hdf5Path.toAbsolutePath(),
			files.size(), is(greaterThan(2)));

		// Make a test for each file
		return files.stream().map(this::createTest);
	}

	abstract protected DynamicNode createTest(Path path);

	public static void recurseGroup(Group group) {
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

	public static void verifyAttributes(Node node) {
		if (node instanceof Link) {
			if (((Link) node).isBrokenLink()) {
				return; // Can't verify broken links
			}
		}
		for (Entry<String, Attribute> entry : node.getAttributes().entrySet()) {
			Attribute attribute = entry.getValue();
			assertThat(attribute.getName(), is(equalTo(entry.getKey())));
			assertThat(attribute.getJavaType(), is(notNullValue()));
			if (attribute.isEmpty()) {
				assertThat(attribute.getSize(), is(equalTo(0L)));
				assertThat(attribute.getSizeInBytes(), is(equalTo(0L)));
				assertThat(attribute.getData(), is(nullValue()));
			} else if (attribute.isScalar()) {
				assertThat(attribute.getSize(), is(equalTo(1L)));
				assertThat(attribute.getSizeInBytes(), is(greaterThan(0L)));
				assertThat(attribute.getData(), is(notNullValue()));
				assertThat(attribute.getBuffer(), is(notNullValue()));
			} else {
				assertThat(attribute.getSize(), is(greaterThan(0L)));
				assertThat(attribute.getSizeInBytes(), is(greaterThan(0L)));
				assertThat(attribute.getData(), is(notNullValue()));
				assertThat(attribute.getBuffer(), is(notNullValue()));
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
	private static void verifyDataset(Dataset dataset, Group group) {
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

		final Object dataFlat = dataset.getDataFlat();

		if (dataset.isEmpty()) {
			assertThat(data, is(nullValue()));
			// Empty so should have 0 size
			assertThat(dataset.getStorageInBytes(), is(equalTo(0L)));
			// Could still have non-zero size in case no storage is allocated
			assertThat(dataset.getStorageInBytes(), is(greaterThanOrEqualTo(0L)));
			assertThat((Object[]) dataFlat, is(emptyArray()));
			assertThat(getDimensions(dataFlat), is(new int[]{0}));
		} else if (dataset.isScalar()) {
			assertThat(toObject(dataset.getDimensions()), is(emptyArray()));
			assertThat(data.getClass(), is(equalTo(dataset.getJavaType())));
			// Should have some size
			assertThat(dataset.getSizeInBytes(), is(greaterThan(0L)));
			assertThat(dataset.getSize(), is(equalTo(1L)));
			assertThat(getDimensions(dataFlat), is(new int[]{1}));
			assertThat(Array.get(dataFlat, 0), is(data));
		} else if (dataset.isCompound()) {
			// Compound datasets are currently returned as maps, maybe a custom CompoundDataset might be better in the future...
			assertThat(data, is(instanceOf(Map.class)));
			Map<String, Object> mapData = (Map<String, Object>) data;
			Map<String, Object> flatMapData = (Map<String, Object>) dataFlat;
			assertThat(mapData, is(not(anEmptyMap())));
			assertThat(dataset.getSizeInBytes(), is(greaterThan(0L)));
			assertThat(dataset.getSize(), is(greaterThan(0L)));
			for (String key : mapData.keySet()) {
				// Don't check nested compound
				if(mapData.get(key).getClass().isArray()) {
					assertThat(flatten(mapData.get(key)), is(equalTo(flatten(flatMapData.get(key)))));
				}
			}

		} else if (dataset.isVariableLength()) {
			assertThat(getDimensions(data)[0], is(equalTo(dims[0])));
			assertThat(dataset.getSizeInBytes(), is(greaterThan(0L)));
			assertThat(dataset.getSize(), is(greaterThan(0L)));
			assertThat(getDimensions(dataFlat)[0], is(Arrays.stream(dataset.getDimensions()).reduce(1, Math::multiplyExact)));
		} else if (dataset.getJavaType().isArray()) {
			// e.g. Opaque dataset
			assertThat(getType(data), is(equalTo(dataset.getJavaType().getComponentType())));
			// Should have some size
			assertThat(dataset.getSizeInBytes(), is(greaterThan(0L)));
			assertThat(dataset.getSize(), is(greaterThan(0L)));
			assertThat(getDimensions(dataFlat)[0], is(Arrays.stream(dataset.getDimensions()).reduce(1, Math::multiplyExact)));
		} else {
			assertThat(getDimensions(data), is(equalTo(dims)));
			assertThat(getType(data), is(equalTo(dataset.getJavaType())));
			// Should have some size
			assertThat(dataset.getSizeInBytes(), is(greaterThan(0L)));
			assertThat(dataset.getSize(), is(greaterThan(0L)));
			assertThat(flatten(data), is(equalTo(flatten(dataFlat))));
			assertThat(getDimensions(dataFlat).length, is(1));
			assertThat(getDimensions(dataFlat)[0], is(Arrays.stream(dataset.getDimensions()).reduce(1, Math::multiplyExact)));
		}

		if (dataset instanceof ContiguousDataset && !dataset.isEmpty()) {
			ContiguousDataset contiguousDataset = (ContiguousDataset) dataset;
			ByteBuffer buffer = contiguousDataset.getBuffer();
			assertThat(buffer, is(notNullValue()));
			assertThat(buffer.capacity(), is(greaterThan(0)));
			assertThat(contiguousDataset.getDataAddress(), is(greaterThan(0L)));
			assertThat(contiguousDataset.getFilters(), is(empty()));

			// Can't slice scalar datasets
			if(!dataset.isScalar()) {
				// Read the whole dataset using the slice method should be the same as the non-sliced
				long[] sliceOffset = new long[dataset.getDimensions().length];
				Object slicedData = contiguousDataset.getData(sliceOffset, dataset.getDimensions());
				// Harder to compare compound. Still read it but don't assert on it
				if(!dataset.isCompound()) {
					assertThat(Objects.deepEquals(data, slicedData), is(true));
				}
			}
		}

		if (dataset instanceof ChunkedDataset && !dataset.isEmpty()) {
			ChunkedDataset chunkedDataset = (ChunkedDataset) dataset;
			// Get the first chunk
			ByteBuffer buffer = chunkedDataset.getRawChunkBuffer(new int[dataset.getDimensions().length]);
			assertThat(buffer, is(notNullValue()));
			assertThat(buffer.capacity(), is(greaterThan(0)));
			assertThat(chunkedDataset.getFilters(),is(notNullValue()));
		}

		Object fillValue = dataset.getFillValue(); // Should return fill value or null if not present
		if(fillValue != null) {
			assertThat(fillValue, is(instanceOf(dataset.getJavaType()))); // check fill value is correct type
		}
	}

	private static Class<?> getType(Object data) {
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
	private static void verifyGroup(Group group) {
		assertThat(group.getAttributes(), is(notNullValue()));
		assertThat(group.isGroup(), is(true));
		assertThat(group.isLink(), is(false));
		assertThat(group.getType(), is(NodeType.GROUP));
	}
}
