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
import io.jhdf.api.Dataset;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.file.Paths;

/**
 * Example application for reading a dataset from HDF5
 *
 * @author James Mudd
 */
public class ReadDataset {
	public static void main(String[] args) {
		try (HdfFile hdfFile = new HdfFile(Paths.get(args[0]))) {
			Dataset dataset = hdfFile.getDatasetByPath(args[1]);
			// data will be a java array of the dimensions of the HDF5 dataset
			Object data = dataset.getData();
			System.out.println(ArrayUtils.toString(data)); //NOSONAR - sout in example
		}
	}
}
