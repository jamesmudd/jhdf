/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import org.junit.jupiter.api.DynamicNode;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * This is a "catch all" test designed to look at all the test HDF5 files and
 * fully explore the tree in all of them. In doing so it exercises most of the
 * jHDF code and validates the behaviour of groups and datasets.
 *
 * @author James Mudd
 */
class TestAllFilesInMemory extends TestAllFilesBase {

	protected DynamicNode createTest(Path path) {
		return dynamicTest(path.getFileName().toString(), () -> {
			try (HdfFile hdfFile = HdfFile.fromBytes(Files.readAllBytes(path))) {
				verifyAttributes(hdfFile);
				recurseGroup(hdfFile);
			}
		});
	}

}
