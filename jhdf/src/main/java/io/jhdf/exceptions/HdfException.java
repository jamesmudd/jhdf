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

/**
 * Superclass of all exceptions thrown by jHDF.
 *
 * @author James Mudd
 */
public class HdfException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HdfException(String message) {
		super(message);
	}

	public HdfException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
