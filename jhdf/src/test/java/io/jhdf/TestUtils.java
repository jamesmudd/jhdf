/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import java.net.URL;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;

public final class TestUtils {
	private TestUtils() {
		throw new AssertionError("No instances of TestUtils");
	}

	public static HdfFile loadTestHdfFile(String fileName) throws Exception {
		URL url = TestUtils.class.getResource("/hdf5/" + fileName);
		if (url == null) {
			throw new RuntimeException("Could not find test file named: " + fileName);
		}
		return new HdfFile(Paths.get(url.toURI()));
	}

	public static double[] toDoubleArray(Object data) {
		return Arrays.stream(Utils.flatten(data))
			.mapToDouble(el -> Double.parseDouble(el.toString()))
			.toArray();
	}

}
