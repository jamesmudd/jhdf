/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ObjectCommentMessageTest {

	private static final String COMMENT = "Test object comment";
	private final BitSet flags = BitSet.valueOf(new byte[1]); // Empty flags
	private ByteBuffer buffer;

	@BeforeEach
	void createBuffer() {
		byte[] bytes = COMMENT.getBytes(US_ASCII);
		buffer = ByteBuffer.allocate(bytes.length + 1);
		buffer.rewind();
		buffer.put(bytes);
		buffer.put(Constants.NULL); // Null terminated
		buffer.rewind();
	}

	@Test
	void testObjectModificationTimeMessage() {
		ObjectCommentMessage message = new ObjectCommentMessage(buffer, flags);
		assertThat(message.getComment(), is(equalTo(COMMENT)));
	}

}
