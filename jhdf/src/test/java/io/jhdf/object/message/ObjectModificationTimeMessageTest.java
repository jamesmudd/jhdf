/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.BitSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectModificationTimeMessageTest {

	private LocalDateTime currentTime;
	private long currentTimeEpocSeconds;
	private ByteBuffer buffer;
	private final BitSet flags = BitSet.valueOf(new byte[1]); // Empty flags

	@BeforeEach
	private void createBuffer() {
		buffer = ByteBuffer.allocate(8);
		buffer.rewind();
		buffer.put((byte) 1); // Version
		buffer.position(4); // Skip 3 bytes
		currentTime = LocalDateTime.now().withNano(0); // No nanos as they are not stored
		currentTimeEpocSeconds = currentTime.toEpochSecond(ZoneOffset.UTC);
		buffer.putInt(Math.toIntExact(currentTimeEpocSeconds));
		buffer.rewind(); // put the buffer back at the start
	}

	@Test
	void testObjectModificationTimeMessage() {
		ObjectModificationTimeMessage message = new ObjectModificationTimeMessage(buffer, flags);
		assertThat(message.getUnixEpocSecond(), is(equalTo(currentTimeEpocSeconds)));
		assertThat(message.getModifiedTime(), is(equalTo(currentTime)));
	}

	@Test
	void testObjectModificationTimeMessageInvalidVersion() {
		buffer.put(0, (byte) 2); // Change the version to 2 should throw
		assertThrows(HdfException.class, () -> new ObjectModificationTimeMessage(buffer, flags));
	}
}
