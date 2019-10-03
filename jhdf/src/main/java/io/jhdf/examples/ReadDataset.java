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
import io.jhdf.api.Dataset;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;

/**
 * Example application for reading a dataset from HDF5
 *
 * @author James Mudd
 */
public class ReadDataset {
	public static void main(String[] args) {
		File file = new File(args[0]);

		try (HdfFile hdfFile = new HdfFile(file)) {
			Dataset dataset = hdfFile.getDatasetByPath(args[1]);
			// data will be a java array of the dimensions of the HDF5 dataset
			Object data = dataset.getData();
			System.out.println(ArrayUtils.toString(data));
		}
	}
}
