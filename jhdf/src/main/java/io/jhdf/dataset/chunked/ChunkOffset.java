/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked;

import java.util.Arrays;

/**
 * Class to wrap an int[] to be used as Map keys
 *
 * @author James Mudd
 */
public final class ChunkOffset {
	private final int[] offset;

	public ChunkOffset(int[] chunkOffset) {
		this.offset = chunkOffset.clone(); // Defensive copy
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChunkOffset that = (ChunkOffset) o;
		return Arrays.equals(offset, that.offset);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(offset);
	}

	@Override
	public String toString() {
		return "ChunkOffset{" + Arrays.toString(offset) + '}';
	}
}
