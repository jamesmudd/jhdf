/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

/**
 * Thrown when checked arithmetic detects an overflow or an invalid arithmetic
 * operation.
 *
 * @author James Mudd
 */
public class HdfArithmeticException extends HdfException {

	private static final long serialVersionUID = 1L;

	public HdfArithmeticException(String message) {
		super(message);
	}

	public HdfArithmeticException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
