package io.jhdf.examples;

import java.io.File;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

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
		}
	}
}
