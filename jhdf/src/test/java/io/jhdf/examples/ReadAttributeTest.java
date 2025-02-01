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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReadAttributeTest {

	@Test
	void testReadingAttributeEarliest() {
		assertDoesNotThrow(() ->
			ReadAttribute.main(new String[]{"src/test/resources/hdf5/test_attribute_earliest.hdf5", "/test_group/data", "2d_string"}));
	}

	@Test
	void testReadingAttributeLatest() {
		assertDoesNotThrow(() ->
			ReadAttribute.main(new String[]{"src/test/resources/hdf5/test_attribute_latest.hdf5", "/test_group/data", "2d_string"}));
	}

}
