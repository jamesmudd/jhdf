/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
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

	public static final byte NULL = '\0';

	public static final byte SPACE = ' ';

	/** All bits on */
	public static final long UNDEFINED_ADDRESS = -1L;

	public static final String PATH_SEPARATOR = "/";

	private Constants() {
	} // No instances
}
