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

import io.jhdf.ObjectHeader.ObjectHeaderV1;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

class ObjectHeaderTest {
	private HdfBackingStorage hdfBackingStorage;
	private Superblock sb;
	private FileChannel fc;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		final URI testFileUri = this.getClass().getResource("/hdf5/test_file.hdf5").toURI();
		fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
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
	void testLazyObjectHeader() throws ConcurrentException, IOException {
		FileChannel spyFc = Mockito.spy(fc);
		HdfBackingStorage hdfBackingStorage = new HdfFileChannel(spyFc, sb);
		LazyInitializer<ObjectHeader> lazyObjectHeader = ObjectHeader.lazyReadObjectHeader(hdfBackingStorage, 10904); // int8
		// header
		// Creating the lazy object header should not touch the file
		Mockito.verifyNoInteractions(spyFc);

		// Get the actual header should cause the file to be read
		lazyObjectHeader.get();

		// Check the file was read
		verify(spyFc, Mockito.atLeastOnce()).read(any(ByteBuffer.class), anyLong());

		// Ensure nothing else was done to the file
		Mockito.verifyNoMoreInteractions(spyFc);
	}

}
