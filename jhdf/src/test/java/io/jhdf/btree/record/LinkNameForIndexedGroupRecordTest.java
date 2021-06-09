/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree.record;

import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkNameForIndexedGroupRecordTest {

	@Test
	void testParsingBuffer() {
		ByteBuffer bb = ByteBuffer.allocate(11);
		bb.putInt(123456); // hash
		bb.put(new byte[]{1, 2, 3, 4, 5, 6, 7}); // id
		bb.rewind();

		LinkNameForIndexedGroupRecord record = new LinkNameForIndexedGroupRecord(bb);
		assertThat(bb.hasRemaining(), is(false));

		assertThat(record.getHash(), is(equalTo(123456L)));
		byte[] id = new byte[7];
		record.getId().get(id);
		assertThat(id, is(equalTo(new byte[]{1, 2, 3, 4, 5, 6, 7})));
	}

	@Test
	void testInvalidLengthBufferThrows() {
		ByteBuffer bb = ByteBuffer.allocate(4);
		assertThrows(HdfException.class, () -> new LinkNameForIndexedGroupRecord(bb));
	}

}
