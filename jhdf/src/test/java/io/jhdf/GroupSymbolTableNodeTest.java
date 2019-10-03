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
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupSymbolTableNodeTest {
	HdfFileChannel hdfFc;

	@BeforeEach
	public void setUp() throws IOException, URISyntaxException {
		final URI testFileUri = this.getClass().getResource("test_file.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfFc = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	public void after() {
		hdfFc.close();
	}

	@Test
	public void testGroupSymbolTableNode() {
		GroupSymbolTableNode node = new GroupSymbolTableNode(hdfFc, 1504);

		assertThat(node.getVersion(), is(equalTo((short) 1)));
		assertThat(node.getNumberOfEntries(), is(equalTo((short) 3)));
		assertThat(node.getSymbolTableEntries().length, is(equalTo(3)));
		assertThat(node.toString(), is(equalTo("GroupSymbolTableNode [address=1504, numberOfEntries=3]")));
	}
}
