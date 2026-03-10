/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

class ImplicitChunkIndexTest {

	@Test
	void supportsLargeImplicitChunkCountsWithoutEagerMaterialization() {
		DatasetInfo datasetInfo = new DatasetInfo(4, new long[]{(long) Integer.MAX_VALUE + 2}, new int[]{1});
		ImplicitChunkIndex chunkIndex = new ImplicitChunkIndex(123L, datasetInfo);

		Collection<Chunk> allChunks = chunkIndex.getAllChunks();
		Iterator<Chunk> iterator = allChunks.iterator();

		assertThat(allChunks.size()).isEqualTo(Integer.MAX_VALUE);
		assertThat(iterator.hasNext()).isTrue();

		Chunk firstChunk = iterator.next();
		assertThat(firstChunk.getAddress()).isEqualTo(123L);
		assertThat(firstChunk.getChunkOffset()).containsExactly(0L);

		Chunk secondChunk = iterator.next();
		assertThat(secondChunk.getAddress()).isEqualTo(127L);
		assertThat(secondChunk.getChunkOffset()).containsExactly(1L);
	}
}
