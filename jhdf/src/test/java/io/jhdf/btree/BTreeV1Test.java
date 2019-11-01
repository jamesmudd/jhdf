/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree;

import io.jhdf.HdfFileChannel;
import io.jhdf.Superblock;
import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BTreeV1Test {
	private HdfFileChannel hdfFc;

	@BeforeEach
    void setUp() throws URISyntaxException, IOException {
		final URI testFileUri = this.getClass().getResource("/hdf5/test_chunked_datasets_earliest.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfFc = new HdfFileChannel(fc, sb);
	}

	@AfterEach
    void after() {
		hdfFc.close();
	}

	@Test
	void testGroupBTreeNode() {
		BTreeV1Group bTree = BTreeV1.createGroupBTree(hdfFc, 136);

		assertThat(bTree.getAddress(), is(equalTo(136L)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(1)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), hasSize(1));
		assertThat(bTree.getChildAddresses(), contains(1504L));
	}

	@Test
	void testDataBTreeNode() {
		BTreeV1Data bTree = BTreeV1.createDataBTree(hdfFc, 2104, 3);

		assertThat(bTree.getAddress(), is(equalTo(2104L)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(20)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), hasSize(20));
		assertThat(bTree.getChunks(), hasSize(20));
	}

	@Test
	void testCreatingBTreeOfDataTypeWithGroupThrows() {
		assertThrows(HdfException.class, () -> BTreeV1.createDataBTree(hdfFc, 136, 1245));
	}

	@Test
	void testCreatingBTreeOfGroupTypeWithDataThrows() {
		assertThrows(HdfException.class, () -> BTreeV1.createGroupBTree(hdfFc, 2104));
	}
}
