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

import io.jhdf.BufferBuilder;
import io.jhdf.Superblock;
import io.jhdf.btree.record.LinkNameForIndexedGroupRecord;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfInMemoryByteBuffer;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BTreeV2Test {

	private static final int HEADER_SIZE = 38;
	private static final int NODE_SIZE = 64;
	private static final int RECORD_SIZE = 11;

	@Test
	void testBTreeV2CycleThrowsHdfException() {
		ByteBuffer file = ByteBuffer.allocate(NODE_SIZE * 2).order(LITTLE_ENDIAN);
		file.put(createHeader(2, NODE_SIZE, 0, 1));
		file.position(NODE_SIZE);
		writeInternalNode(file, NODE_SIZE, NODE_SIZE);

		HdfBackingStorage inMemoryStorage = new HdfInMemoryByteBuffer(file, new Superblock.SuperblockV2V3());

		HdfException exception = assertThrows(HdfException.class,
			() -> new BTreeV2<LinkNameForIndexedGroupRecord>(inMemoryStorage, 0));

		assertThat(exception.getCause(), is(instanceOf(HdfException.class)));
		assertThat(exception.getCause().getMessage(), containsString("cycle"));
	}

	@Test
	void testBTreeV2DepthExceededThrowsHdfException() {
		ByteBuffer file = ByteBuffer.allocate(HEADER_SIZE).order(LITTLE_ENDIAN);
		file.put(createHeader(0xFFFF, NODE_SIZE, 0, 1));

		HdfBackingStorage inMemoryStorage = new HdfInMemoryByteBuffer(file, new Superblock.SuperblockV2V3());

		HdfException exception = assertThrows(HdfException.class,
			() -> new BTreeV2<LinkNameForIndexedGroupRecord>(inMemoryStorage, 0));

		assertThat(exception.getCause(), is(instanceOf(HdfException.class)));
		assertThat(exception.getCause().getMessage(), containsString("maximum depth"));
	}

	private static ByteBuffer createHeader(int depth, int nodeSize, int numberOfRootRecords, long totalRecords) {
		return new BufferBuilder()
			.writeBytes("BTHD".getBytes(StandardCharsets.US_ASCII))
			.writeByte(0) // Version
			.writeByte(5) // Node type
			.writeInt(nodeSize)
			.writeShort(RECORD_SIZE)
			.writeShort(depth)
			.writeByte(100) // Split percent
			.writeByte(50) // Merge percent
			.writeLong(NODE_SIZE) // Root node address
			.writeShort(numberOfRootRecords)
			.writeLong(totalRecords)
			.appendChecksum()
			.build();
	}

	private static void writeInternalNode(ByteBuffer file, int nodeAddress, long childAddress) {
		file.position(nodeAddress);
		file.put("BTIN".getBytes(StandardCharsets.US_ASCII));
		file.put((byte) 0); // Version
		file.put((byte) 5); // Node type
		// No records in this node
		file.putLong(childAddress);
		file.put((byte) 0); // Number of records in the child
		file.put((byte) 0); // Total number of records in the child
	}
}
