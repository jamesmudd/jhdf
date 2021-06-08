/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.dataset.ChunkedDataset;
import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.flatten;
import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ChunkedV4DatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "chunked_v4_datasets.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		hdfFile.close();
	}

	@TestFactory
	Stream<DynamicNode> verifyDatasets() {
		List<Dataset> datasets = new ArrayList<>();
		getAllDatasets(hdfFile, datasets);

		return datasets.stream().map(this::verifyDataset);
	}

	private void getAllDatasets(Group group, List<Dataset> datasets) {
		for (Node node : group) {
			if (node instanceof Group) {
				Group group2 = (Group) node;
				getAllDatasets(group2, datasets);
			} else if (node instanceof Dataset) {
				datasets.add((Dataset) node);
			}
		}
	}

	private DynamicTest verifyDataset(Dataset dataset) {
		return dynamicTest(dataset.getPath(), () -> {
			if (dataset.getName().startsWith("large")) {
				assertThat(dataset.getDimensions(), is(equalTo(new int[]{200, 5, 10})));
			} else {
				assertThat(dataset.getDimensions(), is(equalTo(new int[]{5, 3})));
			}
			Object data = dataset.getData();
			Object[] flatData = flatten(data);
			for (int i = 0; i < flatData.length; i++) {
				// Do element comparison as there are all different primitive numeric types
				assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
			}
		});
	}

	@Test
	void testInvalidChunkOffsetThrows() {
		Dataset dataset = hdfFile.getDatasetByPath("/fixed_array/int32");
		assertThat(dataset, isA(ChunkedDataset.class));
		ChunkedDataset chunkedDataset = (ChunkedDataset) dataset;
		assertThrows(HdfException.class, () -> chunkedDataset.getRawChunkBuffer(new int[]{2, 2}));
	}

	@Test
	void testGettingRawChunk() {
		Dataset dataset = hdfFile.getDatasetByPath("/fixed_array/int32");
		assertThat(dataset, isA(ChunkedDataset.class));
		ChunkedDataset chunkedDataset = (ChunkedDataset) dataset;
		assertThat(toObject(chunkedDataset.getChunkDimensions()), is(arrayContaining(2, 3)));

		ByteBuffer rawChunkBuffer = chunkedDataset.getRawChunkBuffer(new int[]{0, 0});
		assertThat(rawChunkBuffer.capacity(), is(24));

		rawChunkBuffer.order(ByteOrder.LITTLE_ENDIAN); // set byte order to allow verification of values
		IntBuffer intBuffer = rawChunkBuffer.asIntBuffer(); // its an int dataset
		assertThat(intBuffer.capacity(), is(6)); // chunk size is 2x3

		// read the chunk and verify the data
		int[] chunkData = new int[6];
		intBuffer.get(chunkData);
		assertThat(toObject(chunkData), is(arrayContaining(0, 1, 2, 3, 4, 5)));
	}
}
