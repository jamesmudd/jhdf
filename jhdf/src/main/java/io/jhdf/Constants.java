/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

/**
 * Constants used in HDF5 files
 *
 * @author James Mudd
 */
public final class Constants {

	private Constants() {
		throw new AssertionError("No instances of Constants");
	}

	public static final byte NULL = '\0';

	public static final byte SPACE = ' ';

	/** All bits on */
	public static final long UNDEFINED_ADDRESS = -1L;

	public static final String PATH_SEPARATOR = "/";

}
