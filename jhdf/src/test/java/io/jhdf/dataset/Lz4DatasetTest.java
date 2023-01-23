/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.filter.PipelineFilterWithData;
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
import static io.jhdf.filter.BitShuffleFilter.LZ4_COMPRESSION;
import static io.jhdf.filter.BitShuffleFilter.NO_COMPRESSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

class Lz4DatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "lz4_datasets.hdf5";
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
		// Check the filter
		List<PipelineFilterWithData> filters = dataset.getFilters();
		assertThat(filters, hasSize(1));
		PipelineFilterWithData lz4Filter = filters.get(0);
		assertThat(lz4Filter.getId(), is(32004));
		assertThat(lz4Filter.getName(), is("lz4"));

		// Now check the data
		Object data = dataset.getData();

		// convert Data to string list
		List<Double> dataAsDouble = Arrays.stream(StringUtils.split(ArrayUtils.toString(data), ",{}"))
			.map(Double::parseDouble)
			.collect(Collectors.toList());
		assertThat(dataAsDouble, Matchers.contains(EXPECTED_DATA_DOUBLE));
	}

}
