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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.BitSet;

import org.junit.jupiter.api.Test;

class FillValueOldMessageTest {

	@Test
	void testCreatingAndReadingMessage() {
		// 4 bytes for size and 4 for a float
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putInt(4);
		bb.putFloat(123.45f);
		bb.rewind();

		FillValueOldMessage fillValueOldMessage = new FillValueOldMessage(bb, BitSet.valueOf(new byte[1]));

		ByteBuffer fillValueBuffer = fillValueOldMessage.getFillValue();
		assertThat(fillValueBuffer.getFloat(), is(equalTo(123.45f)));

		// Check the buffer is read only
		assertThrows(ReadOnlyBufferException.class, () -> fillValueBuffer.putInt(12));
	}

}
