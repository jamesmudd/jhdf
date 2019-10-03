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
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfInvalidPathException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupTest {
	private static final String DATASETS_GROUP = "datasets_group";

	private HdfFileChannel hdfFc;

	// Mock
	private Group rootGroup;

	@BeforeEach
	public void setUp() throws IOException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		File file = new File(testFileUrl);
		FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);

		hdfFc = new HdfFileChannel(fc, sb);

		rootGroup = mock(Group.class);
		when(rootGroup.getPath()).thenReturn("/");
		when(rootGroup.getFile()).thenReturn(file);
	}

	@AfterEach
	public void after() {
		hdfFc.close();
	}

	@Test
	public void testGroup() {
		Group group = GroupImpl.createGroup(hdfFc, 800, DATASETS_GROUP, rootGroup);
		assertThat(group.getPath(), is(equalTo("/datasets_group/")));
		assertThat(group.toString(), is(equalTo("Group [name=datasets_group, path=/datasets_group/, address=0x320]")));
		assertThat(group.isGroup(), is(true));
		assertThat(group.getChildren().keySet(), hasSize(2));
		assertThat(group.getName(), is(equalTo(DATASETS_GROUP)));
		assertThat(group.getType(), is(equalTo(NodeType.GROUP)));
		assertThat(group.getParent(), is(rootGroup));
		assertThat(group.getAddress(), is(equalTo(800L)));
	}

	@Test
	void testGettingChildrenByName() {
		Group group = GroupImpl.createGroup(hdfFc, 800, DATASETS_GROUP, rootGroup);
		Node child = group.getChild("int");
		assertThat(child, is(notNullValue()));
	}

	@Test
	void testGettingMissingChildReturnsNull() {
		Group group = GroupImpl.createGroup(hdfFc, 800, DATASETS_GROUP, rootGroup);
		Node child = group.getChild("made_up_missing_child_name");
		assertThat(child, is(nullValue()));
	}

	@Test
	void testGetByPathWithInvalidPathReturnsNull() {
		Group group = GroupImpl.createGroup(hdfFc, 800, DATASETS_GROUP, rootGroup);
		assertThrows(HdfInvalidPathException.class, () -> group.getByPath("float/missing_node"));
	}

	@Test
	void testGetByPathWithValidPathReturnsNode() {
		Group group = GroupImpl.createGroup(hdfFc, 800, DATASETS_GROUP, rootGroup);
		String path = "float/float32";
		Node child = group.getByPath(path);
		assertThat(child.getPath(), is(equalTo(group.getPath() + path)));
	}

	@Test
	void testGetByPathThroughDatasetThrows() {
		Group group = GroupImpl.createGroup(hdfFc, 800, DATASETS_GROUP, rootGroup);
		// Try to keep resolving a path through a dataset 'float32' this should return
		// null
		String path = "float/float32/missing_node";
		HdfInvalidPathException e = assertThrows(HdfInvalidPathException.class, () -> group.getByPath(path));
		assertThat(e.getPath(), is(equalTo("/datasets_group/float/float32/missing_node")));
	}
}
