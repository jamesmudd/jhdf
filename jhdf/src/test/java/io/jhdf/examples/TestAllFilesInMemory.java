/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
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
import io.jhdf.api.dataset.ChunkedDataset;
import io.jhdf.api.dataset.ContiguousDataset;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
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
