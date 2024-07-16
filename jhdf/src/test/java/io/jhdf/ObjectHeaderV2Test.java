/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.ObjectHeader.ObjectHeaderV2;
import io.jhdf.object.message.GroupInfoMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import io.jhdf.storage.HdfInMemoryByteBuffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ObjectHeaderV2Test {

	/**
	 * This will need to be updated each time the test files are regenerated
	 */
	private static final long TIMESTAMP = 1553279213L;

	private HdfBackingStorage hdfBackingStorage;

	@BeforeEach
	void setUp() throws IOException, URISyntaxException {
		final URI testFileUri = this.getClass().getResource("/hdf5/test_file2.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfBackingStorage = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	void after() {
		hdfBackingStorage.close();
	}

	@Test
	void testRootGroupObjectHeaderV2() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 48); // Root group header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(48L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	@Disabled("Doesn't handle different chunk size 0 ATM")
	void roundTripRootGroupObjectHeader() {
		ObjectHeaderV2 oh = (ObjectHeaderV2) ObjectHeader.readObjectHeader(hdfBackingStorage, 48);
		ByteBuffer recreated = oh.toBuffer();
		ByteBuffer original = hdfBackingStorage.readBufferFromAddress(48, 150); // This header is 147 bytes
		assertThat(recreated.array(), is(original.array()));
	}

	@Test
	void testDatasetsGroupObjectHeaderV2() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 195); // Root group header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(195L)));
		assertThat(oh.getMessages().size(), is(equalTo(9)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	void testObjectHeaderOnFloat16Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 608); // float16 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(608L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	void testObjectHeaderOnFloat32Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 892); // float32 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(892L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	void testObjectHeaderOnFloat64Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 1176); // float64 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(1176L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	void testObjectHeaderOnInt8Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 1655); // int8 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(1655L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(false));
		assertThat(oh.isAttributeCreationOrderTracked(), is(false));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	void testCreationOrderTracked() throws IOException, URISyntaxException {
		// this test fails without skipping the creation order in Message#readObjectHeaderV2Message
		final URI testFileUri = this.getClass().getResource("/hdf5/test_attribute_with_creation_order.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		HdfBackingStorage hdfBackingStorage = new HdfFileChannel(fc, sb);

		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfBackingStorage, 48);

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(48L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));
		assertThat(oh.isAttributeCreationOrderIndexed(), is(true));
		assertThat(oh.isAttributeCreationOrderTracked(), is(true));

		hdfBackingStorage.close();
	}

	@Test
	void roundtrip() {
		GroupInfoMessage groupInfoMessage = GroupInfoMessage.createBasic();
		LinkInfoMessage linkInfoMessage = LinkInfoMessage.createBasic();
		LinkMessage linkMessage = LinkMessage.create("Group", 99);

		List<Message> messages = new ArrayList<>();
		messages.add(groupInfoMessage);
		messages.add(linkInfoMessage);
		messages.add(linkMessage);
		ObjectHeaderV2 objectHeader = new ObjectHeaderV2(0, messages);

		ByteBuffer buffer = objectHeader.toBuffer();

		HdfInMemoryByteBuffer hdfBackingStorage = new HdfInMemoryByteBuffer(buffer, new Superblock.SuperblockV2V3());
		ObjectHeader readObjectHeader = ObjectHeader.readObjectHeader(hdfBackingStorage, 0);

		Assertions.assertThat(readObjectHeader).usingRecursiveComparison()
			.withStrictTypeChecking()
			.isEqualTo(objectHeader);
	}

}
