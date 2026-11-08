/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree;

import io.jhdf.Superblock;
import io.jhdf.TestUtils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import io.jhdf.storage.HdfInMemoryByteBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BTreeV1Test {

	private static final int NODE_SIZE = 128;

	private HdfBackingStorage hdfBackingStorage;

	@BeforeEach
	void setUp() throws IOException {
		Path testPath = TestUtils.getTestPath("test_chunked_datasets_earliest.hdf5");
		FileChannel fc = FileChannel.open(testPath, StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfBackingStorage = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	void after() {
		hdfBackingStorage.close();
	}

	@Test
	void testGroupBTreeNode() {
		BTreeV1Group bTree = BTreeV1.createGroupBTree(hdfBackingStorage, 136);

		assertThat(bTree.getAddress(), is(equalTo(136L)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(1)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), hasSize(1));
		assertThat(bTree.getChildAddresses(), contains(1504L));
	}

	@Test
	void testDataBTreeNode() {
		BTreeV1Data bTree = BTreeV1.createDataBTree(hdfBackingStorage, 2104, 3);

		assertThat(bTree.getAddress(), is(equalTo(2104L)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(20)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), hasSize(20));
		assertThat(bTree.getChunks(), hasSize(20));
	}

	@Test
	void testCreatingBTreeOfDataTypeWithGroupThrows() {
		assertThrows(HdfException.class, () -> BTreeV1.createDataBTree(hdfBackingStorage, 136, 1245));
	}

	@Test
	void testCreatingBTreeOfGroupTypeWithDataThrows() {
		assertThrows(HdfException.class, () -> BTreeV1.createGroupBTree(hdfBackingStorage, 2104));
	}

	@Test
	void testGroupBTreeCycleThrowsHdfException() {
		HdfBackingStorage inMemoryStorage = createGroupBTreeStorage(1, true);

		HdfException exception = assertThrows(HdfException.class,
			() -> BTreeV1.createGroupBTree(inMemoryStorage, 0));

		assertThat(exception.getMessage(), containsString("cycle"));
	}

	@Test
	void testDataBTreeCycleThrowsHdfException() {
		HdfBackingStorage inMemoryStorage = createDataBTreeStorage(1, true);

		HdfException exception = assertThrows(HdfException.class,
			() -> BTreeV1.createDataBTree(inMemoryStorage, 0, 1));

		assertThat(exception.getMessage(), containsString("cycle"));
	}

	@Test
	void testGroupBTreeDepthExceededThrowsHdfException() {
		HdfBackingStorage inMemoryStorage = createGroupBTreeStorage(100, false);

		HdfException exception = assertThrows(HdfException.class,
			() -> BTreeV1.createGroupBTree(inMemoryStorage, 0));

		assertThat(exception.getMessage(), containsString("maximum depth"));
	}

	@Test
	void testDataBTreeDepthExceededThrowsHdfException() {
		HdfBackingStorage inMemoryStorage = createDataBTreeStorage(100, false);

		HdfException exception = assertThrows(HdfException.class,
			() -> BTreeV1.createDataBTree(inMemoryStorage, 0, 1));

		assertThat(exception.getMessage(), containsString("maximum depth"));
	}

	private static HdfBackingStorage createGroupBTreeStorage(int nodeCount, boolean cycle) {
		ByteBuffer file = ByteBuffer.allocate(nodeCount * NODE_SIZE).order(LITTLE_ENDIAN);
		for (int i = 0; i < nodeCount; i++) {
			final long childAddress = i + 1 < nodeCount
				? (long) (i + 1) * NODE_SIZE
				: (cycle ? 0 : UNDEFINED_ADDRESS);
			writeGroupBTreeNode(file, i * NODE_SIZE, childAddress);
		}
		return new HdfInMemoryByteBuffer(file, new Superblock.SuperblockV2V3());
	}

	private static HdfBackingStorage createDataBTreeStorage(int nodeCount, boolean cycle) {
		ByteBuffer file = ByteBuffer.allocate(nodeCount * NODE_SIZE).order(LITTLE_ENDIAN);
		for (int i = 0; i < nodeCount; i++) {
			final long childAddress = i + 1 < nodeCount
				? (long) (i + 1) * NODE_SIZE
				: (cycle ? 0 : UNDEFINED_ADDRESS);
			writeDataBTreeNode(file, i * NODE_SIZE, childAddress);
		}
		return new HdfInMemoryByteBuffer(file, new Superblock.SuperblockV2V3());
	}

	private static void writeGroupBTreeNode(ByteBuffer file, int nodeAddress, long childAddress) {
		writeBTreeNodeHeader(file, nodeAddress, (byte) 0);
		file.position(nodeAddress + 32); // Key plus child pointer
		file.putLong(childAddress);
	}

	private static void writeDataBTreeNode(ByteBuffer file, int nodeAddress, long childAddress) {
		writeBTreeNodeHeader(file, nodeAddress, (byte) 1);
		file.position(nodeAddress + 48); // Key plus child pointer for one dimension
		file.putLong(childAddress);
	}

	private static void writeBTreeNodeHeader(ByteBuffer file, int nodeAddress, byte nodeType) {
		file.position(nodeAddress);
		file.put("TREE".getBytes(StandardCharsets.US_ASCII));
		file.put(nodeType);
		file.put((byte) 1); // Non-leaf node
		file.putShort((short) 1); // One entry
		file.putLong(UNDEFINED_ADDRESS);
		file.putLong(UNDEFINED_ADDRESS);
	}
}
