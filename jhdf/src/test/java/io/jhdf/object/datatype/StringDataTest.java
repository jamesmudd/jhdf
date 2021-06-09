/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Constants;
import io.jhdf.object.datatype.StringData.NullPadded;
import io.jhdf.object.datatype.StringData.SpacePadded;
import io.jhdf.object.datatype.StringData.StringPaddingHandler;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StringDataTest {

	private static final char NULL_AS_CHAR = (char) Constants.NULL;

	@Test
	void testNullTerminated() {
		StringPaddingHandler nullTerminated = new StringData.NullTerminated();

		ByteBuffer byteBuffer = ByteBuffer.allocate(20);

		byteBuffer.put("hello".getBytes(US_ASCII));
		byteBuffer.put(Constants.NULL); // Null terminate it
		byteBuffer.put("again".getBytes(US_ASCII)); // This should be cropped off because its after a null
		byteBuffer.rewind();

		assertThat(byteBuffer.limit(), is(20)); // length of buffer
		nullTerminated.setBufferLimit(byteBuffer);
		assertThat(byteBuffer.limit(), is(5)); // hello is 5 chars

		// Read the string back and check the value
		assertThat(US_ASCII.decode(byteBuffer).toString(), is("hello"));
	}

	@Test
	void testNullTerminatedWithFullBuffer() {
		StringPaddingHandler nullTerminated = new StringData.NullTerminated();

		ByteBuffer byteBuffer = ByteBuffer.allocate(5);

		byteBuffer.put("hello".getBytes(US_ASCII));
		// No null terminater as the string fills the full buffer
		byteBuffer.rewind();

		assertThat(byteBuffer.limit(), is(5)); // length of buffer
		nullTerminated.setBufferLimit(byteBuffer);
		assertThat(byteBuffer.limit(), is(5)); // hello is 5 chars

		// Read the string back and check the value
		assertThat(US_ASCII.decode(byteBuffer).toString(), is("hello"));
	}

	@Test
	void testNullPadded() {
		StringPaddingHandler nullPadded = new NullPadded();

		ByteBuffer byteBuffer = ByteBuffer.allocate(20);

		byteBuffer.put("hello".getBytes(US_ASCII));
		byteBuffer.put(Constants.NULL); // Stick a null in the middle
		byteBuffer.put("again".getBytes(US_ASCII)); // This should be included as its not on the end
		byteBuffer.put(Constants.NULL); // a null at the end
		byteBuffer.rewind();

		assertThat(byteBuffer.limit(), is(20)); // length of buffer
		nullPadded.setBufferLimit(byteBuffer);
		assertThat(byteBuffer.limit(), is(11)); // hello + null + again is 11 chars

		// Read the string back and check the value
		assertThat(US_ASCII.decode(byteBuffer).toString(), is("hello" + NULL_AS_CHAR + "again"));

	}

	@Test
	void testSpacePadded() {
		StringPaddingHandler spacePadded = new SpacePadded();

		ByteBuffer byteBuffer = ByteBuffer.allocate(20);

		byteBuffer.put("hello again         ".getBytes(US_ASCII)); // space in the middle and at the end
		byteBuffer.rewind();

		assertThat(byteBuffer.limit(), is(20)); // length of buffer
		spacePadded.setBufferLimit(byteBuffer);
		assertThat(byteBuffer.limit(), is(11)); // "hello again" is 11 chars

		// Read the string back and check the value
		assertThat(US_ASCII.decode(byteBuffer).toString(), is("hello again"));

	}

	@Test
	void testNullPaddedEmptyString() {
		StringPaddingHandler nullPadded = new NullPadded();

		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		// consists of nulls
		for (int i = 0; i < byteBuffer.capacity(); i++) {
			byteBuffer.put(Constants.NULL);
		}
		byteBuffer.rewind();

		assertThat(byteBuffer.limit(), is(4)); // length of buffer
		nullPadded.setBufferLimit(byteBuffer);
		assertThat(byteBuffer.limit(), is(0)); // string is empty

		// Read the string back and check the value
		assertThat(US_ASCII.decode(byteBuffer).toString(), is(""));
	}

	@Test
	void testSpacePaddedEmptyString() {
		StringPaddingHandler spacePadded = new SpacePadded();

		ByteBuffer byteBuffer = ByteBuffer.allocate(4);

		byteBuffer.put("    ".getBytes(US_ASCII)); // only spaces
		byteBuffer.rewind();

		assertThat(byteBuffer.limit(), is(4)); // length of buffer
		spacePadded.setBufferLimit(byteBuffer);
		assertThat(byteBuffer.limit(), is(0)); // string is empty

		// Read the string back and check the value
		assertThat(US_ASCII.decode(byteBuffer).toString(), is(""));
	}
}
