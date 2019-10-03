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

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    public static Object[] flatten(Object[] data) {
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
        dims.add(Array.getLength(data));

        while (Array.get(data, 0).getClass().isArray()) {
            data = Array.get(data, 0);
            dims.add(Array.getLength(data));
        }
        return ArrayUtils.toPrimitive(dims.toArray(new Integer[0]));
    }
}
