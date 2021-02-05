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

import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GlobalHeapTest {

	private GlobalHeap globalHeap;
	private Superblock sb;
	private HdfBackingStorage hdfFc;

	@BeforeEach
	void setup() throws IOException, URISyntaxException {
		URI testFile = this.getClass().getResource("/hdf5/test_file.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFile), StandardOpenOption.READ);
		sb = Superblock.readSuperblock(fc, 0);
		hdfFc = new HdfFileChannel(fc, sb);

		globalHeap = new GlobalHeap(hdfFc, 2048);
	}

	@AfterEach
	void tearDown() {
		hdfFc.close();
	}

	@Test
	void testGettingObject() {
		ByteBuffer bb = globalHeap.getObjectData(1);

		assertThat(bb.limit(), is(equalTo(19)));
		//  Decode the data
		String data = US_ASCII.decode(bb).toString();
		assertThat(data, is(equalTo("my string attribute")));

	}

	@Test
	void testInvalidSignatureThrows() {
		// Give address of local heap
		assertThrows(HdfException.class, () -> new GlobalHeap(hdfFc, 1384));
	}

	@Test
	void testGettingObjectOutOfRangeThrows() {
		assertThrows(IllegalArgumentException.class, () -> globalHeap.getObjectData(5));
	}

	@Test
	void testGettingReferencesToObject() {
		assertThat(globalHeap.getObjectReferenceCount(1), is(equalTo(0)));
	}

	@Test
	void testGettingReferencesToObjectOutOfRangeThrows() {
		assertThrows(IllegalArgumentException.class, () -> globalHeap.getObjectReferenceCount(5));
	}

	@Test
	void testToString() {
		assertThat(globalHeap.toString(), is(equalTo("GlobalHeap [address=2048, objects=1]")));
	}

	@SuppressWarnings("SameReturnValue")
	@Test
	void testInvalidVersionThrows() throws IOException {
		FileChannel mockFc = Mockito.mock(FileChannel.class);
		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = invocation.getArgument(0);
			bb.rewind();
			bb.put("GCOL".getBytes(US_ASCII)); // Match signature
			bb.put((byte) 4); // mismatch version
			return null;
		}).when(mockFc).read(ArgumentMatchers.any(ByteBuffer.class), ArgumentMatchers.anyLong());

		HdfBackingStorage hdfBackingStorage = new HdfFileChannel(mockFc, sb);
		assertThrows(HdfException.class, () -> new GlobalHeap(hdfBackingStorage, 0));
	}

	@Test
	void testDifferentObjectZero() throws Exception {
		HdfFile file = TestUtils.loadTestHdfFile("globalheaps_test.hdf5");
			Object data = file.getAttribute("attribute").getData();
			assertThat((String[]) data, is(arrayContaining(
					"value0", "value1", "value2", "value3", "value4", "value5", "value6", "")));
	}
}
