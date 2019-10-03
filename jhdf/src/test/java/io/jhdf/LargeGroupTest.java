/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;

public class LargeGroupTest {

	@TestFactory
	Collection<DynamicNode> datasetReadTests() {
		return Arrays.asList(dynamicTest("earliest", createTest("test_large_group_earliest.hdf5")),
				dynamicTest("latest", createTest("test_large_group_latest.hdf5")));
	}

	private Executable createTest(String file) {
		return () -> {
			try (HdfFile hdfFile = new HdfFile(new File(this.getClass().getResource(file).getFile()))) {
				Group largeGroup = (Group) hdfFile.getByPath("large_group");
				assertThat(largeGroup.getChildren().size(), is(equalTo(1000)));

				for (int i = 0; i < 1000; i++) {
					Node node = largeGroup.getChild("data" + i);
					assertThat(node.getName(), is(equalTo("data" + i)));
					assertThat(node, instanceOf(Dataset.class));
					Dataset dataset = (Dataset) node;
					int[] data = (int[]) dataset.getData();
					assertThat(data, is(equalTo(new int[] { i })));
				}
			}
		};
	}

}
