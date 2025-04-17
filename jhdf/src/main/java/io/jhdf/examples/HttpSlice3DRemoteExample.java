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
import io.jhdf.storage.HttpSeekableByteChannel;

import java.net.URL;
import java.util.Arrays;

/**
 * Demonstration of reading remote HDF5 files over HTTP using {@link HttpSeekableByteChannel}
 * and the new {@link HdfFile} constructor.
 *
 * <p>This example opens a publicly hosted `.hdf5` file from a remote URL, without downloading
 * the entire file. It uses HTTP range requests and an LRU cache to read only the necessary
 * bytes on demand.
 *
 * <p>The demo:
 * <ul>
 *   <li>Opens the HDF5 file using {@code new HdfFile(URL)} whcih initializes a
 *   {@code HttpRangeSeekableByteChannel} with LRU caching</li>
 *   <li>Loads the dataset at {@code /resolutions/1000/bins/start}</li>
 *   <li>Reads the dataset in slices of 1000 rows using {@code getData(offset, shape)}</li>
 *   <li>Prints the shape, the first few values, and the total values read</li>
 * </ul>
 *
 * <p>This demonstrates and validates support for:
 * <ul>
 *   <li>Remote access to chunked HDF5 datasets via HTTP</li>
 *   <li>Streaming partial data without needing to download the full file</li>
 * </ul>
 *
 * <p>This is useful for efficiently working with large HDF5 datasets hosted
 * on cloud or institutional storage without requiring full local copies.
 */

public class HttpSlice3DRemoteExample {

	public static void main(String[] args) throws Exception {
		// Public HDF5 dataset hosted on Zenodo
		// Kansal et. al., Particle Cloud Generation with Message Passing Generative Adversarial Networks,
		// NeurIPS 2021 arXiv:2106.11535
		URL url = new URL("https://zenodo.org/records/6975118/files/g.hdf5");

		System.out.println("Opening remote HDF5 via HTTP range: " + url);
		long t0 = System.nanoTime();
		try (HdfFile hdf = new HdfFile(url)) {
			String datasetPath = "/particle_features";
			Dataset ds = hdf.getDatasetByPath(datasetPath);

			long t1 = System.nanoTime();
			System.out.printf("Opened HDF5 File: %s [%.2f ms]%n", datasetPath, (t1 - t0) / 1e6);
			int[] dims = ds.getDimensions();
			System.out.println("Dimensions: " + Arrays.toString(dims));  // [177252, 30, 4]

			int totalLength = dims[0];
			int sliceSize = 100;
			int numSlices = (totalLength + sliceSize - 1) / sliceSize;
			long totalValuesRead = 0;

			// Let's read 5 slices
			for (int i = 0; i < Math.min(numSlices, 5); i++) {
				// offset into the 3D dataset: start at slice i, then full 2nd+3rd dims
				long[] offset = {i * sliceSize, 0, 0};
				// shape of this slice: sliceLength × dims[1] × dims[2]
				int sliceLength = Math.min(sliceSize, totalLength - i);
				int[] shape = {sliceLength, dims[1], dims[2]};

				// getData returns a flattened primitive array in row-major order
				Object raw = ds.getData(offset, shape);

				// Cast raw into a 3D float array
				float[][][] array = (float[][][]) raw;

				// Count how many values we got: sliceLength * 30 * 4
				int valuesRead = array.length * array[0].length * array[0][0].length;
				totalValuesRead += valuesRead;
				System.out.printf("Read slice of shape: [%d, %d, %d] containing %d float32 values\n", shape[0], shape[1], shape[2], valuesRead);

				// Print the first 5 floats of the very first slice
				if (i == 0) {
					System.out.print("First 5 values: ");
					for (int j = 0; j < 5; j++) {
						System.out.printf("%.2f ", array[j][0][0]);
					}
					System.out.println();
				}
			}

			long t2 = System.nanoTime();
			System.out.printf("Read total values: %d from sliced dataset [%.2f ms]%n", totalValuesRead, (t2 - t1) / 1e6);
		}
	}
}
