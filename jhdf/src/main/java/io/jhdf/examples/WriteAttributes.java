/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritiableDataset;

import java.nio.file.Paths;

/**
 * Example application for writing attributes
 *
 * @author James Mudd
 */
public class WriteAttributes {
	public static void main(String[] args) {
		try (WritableHdfFile hdfFile = HdfFile.write(Paths.get("attributes.hdf5"))) {
			// write scalar attribute on the root group
			hdfFile.putAttribute("example_group_attribute", 55.543);

			// Write a dataset
			WritiableDataset dataset = hdfFile.putDataset("ints", new int[]{1, 2, 3, 4});
			dataset.putAttribute("example_ints_attribute", new int[] {1, 2, 3, 4});
		}
	}
}
