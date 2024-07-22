/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.exceptions;

public class HdfWritingException extends HdfException {

	public HdfWritingException(String message) {
		super(message);
	}

	public HdfWritingException(String message, Throwable e) {
		super(message, e);
	}
}
