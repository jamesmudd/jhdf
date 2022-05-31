/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.MatcherAssert.assertThat;

class BitshuffleDatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "bitshuffle_datasets.hdf5";
	private static final Double[] EXPECTED_DATA_DOUBLE = IntStream.range(0,20).mapToDouble(Double::valueOf).boxed().toArray(Double[]::new);

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		hdfFile.close();
	}

	static Stream<Arguments> testBitfieldDataset() {
		return hdfFile.getChildren().values().stream()
			.filter(Dataset.class::isInstance)
			.map(Dataset.class::cast)
			.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource
	void testBitfieldDataset(Dataset dataset) {
		// Now check the data
		Object data = dataset.getData();

		// convert Data to string list
		List<Double> dataAsDouble = Arrays.asList(StringUtils.split(ArrayUtils.toString(data), ",{}")).stream().map(Double::parseDouble).collect(Collectors.toList());
		assertThat(dataAsDouble, Matchers.contains(EXPECTED_DATA_DOUBLE));
	}

}
