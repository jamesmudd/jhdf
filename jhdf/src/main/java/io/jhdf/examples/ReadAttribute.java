/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;

/**
 * Example application for reading an attribute from HDF5
 *
 * @author James Mudd
 */
public class ReadAttribute {

	/**
	 * @param args ["path/to/file.hdf5", "path/to/node", "attributeName"]
	 */
	public static void main(String[] args) {
		File file = new File(args[0]);

		try (HdfFile hdfFile = new HdfFile(file)) {
			Node node = hdfFile.getByPath(args[1]);
			Attribute attribute = node.getAttribute(args[2]);
			Object attributeData = attribute.getData();
			System.out.println(ArrayUtils.toString(attributeData));
		}
	}
}
