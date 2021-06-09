/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class GroupSymbolTableNodeTest {
	private HdfBackingStorage hdfBackingStorage;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		final URI testFileUri = this.getClass().getResource("/hdf5/test_file.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfBackingStorage = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	void after() {
		hdfBackingStorage.close();
	}

	@Test
	void testGroupSymbolTableNode() {
		GroupSymbolTableNode node = new GroupSymbolTableNode(hdfBackingStorage, 1504);

		assertThat(node.getVersion(), is(equalTo((short) 1)));
		assertThat(node.getNumberOfEntries(), is(equalTo((short) 3)));
		assertThat(node.getSymbolTableEntries().length, is(equalTo(3)));
		assertThat(node.toString(), is(equalTo("GroupSymbolTableNode [address=1504, numberOfEntries=3]")));
	}
}
