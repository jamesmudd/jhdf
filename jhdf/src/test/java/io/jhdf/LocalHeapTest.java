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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class LocalHeapTest {
	private HdfFileChannel hdfFc;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		final URI testFileUri = this.getClass().getResource("/hdf5/test_file.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfFc = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	void after() {
		hdfFc.close();
	}

	@Test
	void testLocalHeap() {
		LocalHeap heap = new LocalHeap(hdfFc, 680);

		assertThat(heap.getVersion(), is(equalTo((short) 0)));
		assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
		assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(56L)));
		assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
		assertThat(heap.toString(), is(equalTo(
				"LocalHeap [address=0x2a8, version=0, dataSegmentSize=88, offsetToHeadOfFreeList=56, addressOfDataSegment=0x2c8]")));
	}

	@Test
	void testAccessingData() {
		LocalHeap heap = new LocalHeap(hdfFc, 680);
		ByteBuffer bb = heap.getDataBuffer();
		assertThat(bb.capacity(), is(equalTo(88)));
		// Test reading a name from the heap
		bb.position(8);
		assertThat(Utils.readUntilNull(bb), is("datasets_group"));
	}
}
