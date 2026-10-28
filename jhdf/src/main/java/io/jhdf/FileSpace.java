/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

/**
 * Hands out addresses in a file being written.
 * <p>
 * A dataset streaming its chunks needs somewhere to put them before the tree describing the file exists, so it
 * cannot simply write at a position derived from its own object header. This is the file saying where there is room.
 */
interface FileSpace {

	/**
	 * @return the next address not yet spoken for, without reserving anything. Useful where a structure has to
	 * record the address it will be written at, so its size is only known once built.
	 */
	long nextAddress();

	/**
	 * Reserves space at the next free address.
	 *
	 * @param bytes the number of bytes to reserve
	 * @return the address the reserved space starts at
	 */
	long reserve(long bytes);
}
