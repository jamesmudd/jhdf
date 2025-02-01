/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import java.nio.ByteOrder;

public final class JhdfInfo {

	private JhdfInfo() {
		throw new AssertionError("No instances of JhdfInfo");
	}

	public static final String VERSION = JhdfInfo.class.getPackage().getImplementationVersion();

	public static final String OS = System.getProperty("os.name");

	public static final String ARCH = System.getProperty("os.arch");

	public static final String BYTE_ORDER = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN) ? "LE" : "BE";
}
