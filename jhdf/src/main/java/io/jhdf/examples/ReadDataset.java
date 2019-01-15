package io.jhdf.examples;

import java.io.File;
import java.util.Arrays;

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
		System.out.println(file.getName());

		try (HdfFile hdfFile = new HdfFile(file)) {
			Dataset dataset = hdfFile.getDatasetByPath("/nD_Datasets/3D_float32");
			float[][][] arrayData = (float[][][]) dataset.getData();
			System.out.println(Arrays.deepToString(arrayData));
		}
	}
}
