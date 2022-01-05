/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

/**
 * Exception to be thrown if an operation is attempted on an in-memory file which only makes sense for a file backed
 * file.
 *
 * @author James Mudd
 */
public class InMemoryHdfException extends HdfException {

	public InMemoryHdfException() {
		super("This is an in-memory HDF file");
	}
}
