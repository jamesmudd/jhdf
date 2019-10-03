/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

public class UnsupportedHdfException extends HdfException {

	private static final long serialVersionUID = 1L;

	public UnsupportedHdfException(String message) {
		super(message);
	}

}
