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

import java.util.Collection;
import java.util.Collections;

public class EmptyChunkIndex implements ChunkIndex {

	@Override
	public Collection<Chunk> getAllChunks() {
		return Collections.emptyList();
	}
}
