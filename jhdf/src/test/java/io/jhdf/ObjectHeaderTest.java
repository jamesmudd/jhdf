/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.ObjectHeader.ObjectHeaderV1;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

class ObjectHeaderTest {
	private HdfBackingStorage hdfBackingStorage;
	private Superblock sb;
	private FileChannel fc;

	@BeforeEach
	void setUp() throws IOException {
		Path testPath = TestUtils.getTestPath("test_file.hdf5");
		fc = FileChannel.open(testPath, StandardOpenOption.READ);
		sb = Superblock.readSuperblock(fc, 0);
		hdfBackingStorage = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	void after() throws IOException {
		hdfBackingStorage.close();
		fc.close();
	}

	@Test
	void testObjectHeaderOnGroup() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 800); // dataset_group header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(800L)));
		assertThat(oh.getMessages().size(), is(equalTo(7)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	void testObjectHeaderOnFloat32Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 7272); // float32 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(7272L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	void testObjectHeaderOnFloat64Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 7872); // float64 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(7872L)));
		assertThat(oh.getMessages().size(), is(equalTo(7)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	void testObjectHeaderOnInt8Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 10904); // int8 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(10904L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		// Two refs this one and a hard link /links/hard_link_to_int8
		assertThat(ohV1.getReferenceCount(), is(equalTo(2)));
	}

	@Test
	void testSelfReferencingContinuationMessageThrowsHdfException() throws IOException {
		// Regression test for a malformed v1 object header whose continuation message points back at its own
		// message block. Previously this caused unbounded recursion and a StackOverflowError. The file is
		// space_padding_problem.hdf5 with the message count set to 0xFFFF and the continuation offset set to 112.
		Path testPath = TestUtils.getResourcePath("/malformed/recursive_continuation_v1.hdf5");
		FileChannel fc = FileChannel.open(testPath, StandardOpenOption.READ);
		HdfBackingStorage malformedFileStorage = new HdfFileChannel(fc, Superblock.readSuperblock(fc, 0));
		try {
			HdfException exception = assertThrows(HdfException.class,
				() -> ObjectHeader.readObjectHeader(malformedFileStorage, 96));

			assertThat(exception.getCause(), is(instanceOf(HdfException.class)));
			assertThat(exception.getCause().getMessage(), containsString("already been read"));
		} finally {
			malformedFileStorage.close();
			fc.close();
		}
	}

	@Test
	void testLazyObjectHeader() throws ConcurrentException, IOException {
		FileChannel mockFc = Mockito.mock(FileChannel.class);
		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = invocation.getArgument(0);
			long address = invocation.getArgument(1);
			bb.put(hdfBackingStorage.readBufferFromAddress(address, bb.capacity()));
			return bb.capacity();
		}).when(mockFc).read(any(ByteBuffer.class), anyLong());
		// Reads are bounds checked against the size of the file
		Mockito.when(mockFc.size()).thenReturn(hdfBackingStorage.size());
		HdfBackingStorage hdfBackingStorage = new HdfFileChannel(mockFc, sb);
		LazyInitializer<ObjectHeader> lazyObjectHeader = ObjectHeader.lazyReadObjectHeader(hdfBackingStorage, 10904); // int8

		// header
		// Creating the lazy object header should not touch the file
		Mockito.verifyNoInteractions(mockFc);

		// Get the actual header should cause the file to be read
		lazyObjectHeader.get();

		// Check the file was read
		verify(mockFc, Mockito.atLeastOnce()).read(any(ByteBuffer.class), anyLong());
		// And the size of the file was checked to bounds check the reads
		verify(mockFc, Mockito.atLeastOnce()).size();

		// Ensure nothing else was done to the file
		Mockito.verifyNoMoreInteractions(mockFc);
	}

}
