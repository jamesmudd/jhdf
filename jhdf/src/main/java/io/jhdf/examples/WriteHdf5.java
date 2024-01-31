/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritableGroup;

import java.nio.file.Paths;

/**
 * Example application for writing a simple HDF5 file
 *
 * @author James Mudd
 */
public class WriteHdf5 {
	public static void main(String[] args) {
		try (WritableHdfFile hdfFile = HdfFile.write(Paths.get("jhdf.hdf5"))) {
			hdfFile.putDataset("ints", new int[] {1, 2, 3, 4});
			hdfFile.putDataset("doubles", new double[] {1.0, 2.0, 3.0, 4.0});

			WritableGroup multiDimGroup = hdfFile.putGroup("multiDim");
			multiDimGroup.putDataset("2d-ints", new int[][]{
				{1, 2},
				{3, 4}
			});
			multiDimGroup.putDataset("3d-ints", new int[][][]{
				{
					{1, 2},
					{3, 4}
				},
				{
					{5, 6},
					{7, 8}
				},
			});
		}
	}
}
