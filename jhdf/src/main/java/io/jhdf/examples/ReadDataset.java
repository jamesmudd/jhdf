package io.jhdf.examples;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

public class ReadDataset {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		File file = new File(args[0]);
		System.out.println(file.getName());

		try (HdfFile hdfFile = new HdfFile(file)) {
			Dataset dataset = hdfFile.getDatasetByPath("/datasets_group/float/float64");
			System.out.println(Arrays.toString((double[]) dataset.getData()));
		}

		System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");
	}

}
