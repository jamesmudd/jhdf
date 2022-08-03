/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import org.apache.commons.lang3.ArrayUtils;

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

	public static int[] getDimensions(Object data) {
		List<Integer> dims = new ArrayList<>();
		int dimLength = Array.getLength(data);
		dims.add(dimLength);

		while (dimLength > 0 && Array.get(data, 0).getClass().isArray()) {
			data = Array.get(data, 0);
			dims.add(Array.getLength(data));
		}
		return ArrayUtils.toPrimitive(dims.toArray(new Integer[0]));
	}
}
