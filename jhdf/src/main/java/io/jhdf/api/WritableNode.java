/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.api;

import io.jhdf.storage.HdfFileChannel;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WritableNode extends Node {

	/**
	 * Writes this node to the file. Not for general use
	 *
	 * @param hdfFileChannel The file channel to write this node to
	 * @param position The position in the file to write this node
	 * @return bytes written
	 */
	long write(HdfFileChannel hdfFileChannel, long position);

	/**
	 * Adds an attribute to this node
	 *
	 * @since v0.8.0
	 * @param name The attributes name
	 * @param data The attributes data
	 * @return The attribute
	 */
	Attribute putAttribute(String name, Object data);

	/**
	 * Removes an attribute from this node
	 *
	 * @since v0.8.0
	 * @param name The attribute to remove
	 * @return The removed attribute
	 */
	Attribute removeAttribute(String name);
}
