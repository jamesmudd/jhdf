/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

	public static Object[] flatten(Object data) {
		List<Object> flat = new ArrayList<>();
		flattenInternal(data, flat);
		return flat.toArray();
	}

	private static void flattenInternal(Object data, List<Object> flat) {
		int length = Array.getLength(data);
		for (int i = 0; i < length; i++) {
			Object element = Array.get(data, i);
			if (element.getClass().isArray()) {
				flattenInternal(element, flat);
			} else {
				flat.add(element);
			}
		}
	}

}
