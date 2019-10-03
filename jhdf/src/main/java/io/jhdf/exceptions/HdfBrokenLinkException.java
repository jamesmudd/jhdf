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
 * Thrown when a broken link is attempted to be resolved.
 *
 * @author James Mudd
 */
public class HdfBrokenLinkException extends HdfException {

	private static final long serialVersionUID = 1L;

	public HdfBrokenLinkException(String message, Throwable cause) {
		super(message, cause);
	}

}
