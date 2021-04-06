/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class ChunkOffsetTest {

	@Test
	void testEquals() {
		ChunkOffset chunkOffset1 = new ChunkOffset(new int[]{1, 2, 3});
		ChunkOffset chunkOffset2 = new ChunkOffset(new int[]{1, 2, 3});
		ChunkOffset chunkOffset3 = new ChunkOffset(new int[]{1, 2, 6});

		assertThat(chunkOffset1, equalTo(chunkOffset1));
		assertThat(chunkOffset1, equalTo(chunkOffset2));
		assertThat(chunkOffset1, not(equalTo(chunkOffset3)));
		assertThat(chunkOffset1, not(equalTo(null)));
	}

	@Test
	void testToString() {
		ChunkOffset chunkOffset = new ChunkOffset(new int[]{1, 2, 3});
		assertThat(chunkOffset.toString(), is("ChunkOffset{[1, 2, 3]}"));
	}
}
