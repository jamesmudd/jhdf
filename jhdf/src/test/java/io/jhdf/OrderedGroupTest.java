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

import io.jhdf.api.Group;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

class OrderedGroupTest {
	private static final String ORDERED_GROUP = "ordered_group";
	private static final String UNORDERED_GROUP = "unordered_group";

	private static final String HDF5_TEST_FILE_NAME = "test_ordered_group_latest.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() {
		String earliestTestFileUrl = AttributesTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();
		hdfFile = new HdfFile(new File(earliestTestFileUrl));
	}

	@AfterAll
	static void after() {
		hdfFile.close();
	}

	@Test
    void testOrderedGroup() {
		Group group = (Group) hdfFile.getByPath(ORDERED_GROUP);
		assertThat(group.isLinkCreationOrderTracked(), is(true));
		assertThat(group.getChildren().keySet(), contains("z", "h", "a"));
	}

	@Test
    void testUnOrderedGroup() {
		Group group = (Group) hdfFile.getByPath(UNORDERED_GROUP);
		assertThat(group.isLinkCreationOrderTracked(), is(false));
		assertThat(group.getChildren().keySet(), containsInAnyOrder("z", "h", "a"));
	}

}
