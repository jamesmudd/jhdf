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

import io.jhdf.checksum.ChecksumUtils;
import io.jhdf.checksum.JenkinsLookup3HashLittle;
import io.jhdf.dataset.CompoundDatasetReader;
import io.jhdf.dataset.DatasetLoader;
import io.jhdf.dataset.DatasetReader;
import io.jhdf.dataset.EnumDatasetReader;
import io.jhdf.dataset.VariableLengthDatasetReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.powermock.reflect.Whitebox.invokeConstructor;

class NoInstancesTest {

	static Stream<Arguments> noInstances() {
		return Stream.of(
			Arguments.of(DatasetLoader.class),
			Arguments.of(DatasetReader.class),
			Arguments.of(CompoundDatasetReader.class),
			Arguments.of(EnumDatasetReader.class),
			Arguments.of(VariableLengthDatasetReader.class),
			Arguments.of(Constants.class),
			Arguments.of(Utils.class),
			Arguments.of(JenkinsLookup3HashLittle.class),
			Arguments.of(ChecksumUtils.class)
		);
	}

	@ParameterizedTest
	@MethodSource
	void noInstances(Class<?> clazz) {
		assertThrows(AssertionError.class, () -> invokeConstructor(clazz));
	}

}
