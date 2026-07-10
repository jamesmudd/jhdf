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
import io.jhdf.api.DatasetCreationOptions;
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

			// Chunked and compressed dataset
			double[][] bigData = new double[200][100];
			for (int i = 0; i < bigData.length; i++) {
				for (int j = 0; j < bigData[i].length; j++) {
					bigData[i][j] = Math.sqrt(i) * j;
				}
			}
			hdfFile.putDataset("compressed", bigData, DatasetCreationOptions.builder()
				.chunkDimensions(50, 50)
				.shuffle()
				.deflate(6)
				.build());
		}
	}
}
