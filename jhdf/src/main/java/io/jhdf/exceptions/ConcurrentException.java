/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

/**
 * Checked exception thrown by {@link io.jhdf.LazyInitializer#initialize()} to report an
 * error which occurred while lazily initializing a value.
 *
 * @author James Mudd
 */
public class ConcurrentException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConcurrentException(Throwable cause) {
		super(cause);
	}

	public ConcurrentException(String message, Throwable cause) {
		super(message, cause);
	}
}
