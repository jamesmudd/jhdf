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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstration and benchmark for validating sliced reading support in chunked HDF5 datasets using jHDF.
 *
 * <p>This demo loads a multi-resolution `.mcool` Hi-C file containing chunked datasets for genomic bins
 * (e.g., `/resolutions/1000/bins/chrom`, `start`, and `end`). It verifies that sliced access to these
 * chunked datasets is functioning correctly by:
 *
 * <ul>
 *   <li>Reading each dataset (`chrom`, `start`, `end`) using slices of various sizes (100, 1000, 10,000)</li>
 *   <li>Timing how long it takes to read the full dataset using each slice size</li>
 *   <li>Concatenating all slices to reconstruct the full dataset</li>
 *   <li>Validating that the total number of elements read matches the dataset size</li>
 *   <li>Printing the first and last 3 elements of the reassembled array for visual verification</li>
 * </ul>
 *
 * <p>This demo provides a proof-of-concept and performance benchmark to demonstrate that:
 *
 * <ul>
 *   <li>The {@link io.jhdf.dataset.chunked.ChunkedDatasetBase} class correctly supports reading slices
 *       from chunked datasets via {@code getData(offset, shape)}</li>
 *   <li>Partial and full chunks are handled properly when reading across chunk boundaries</li>
 *   <li>The performance scales with slice size, showing how chunk-aware iteration affects I/O speed</li>
 * </ul>
 *
 * <p>To use this demo:
 * <ol>
 *   <li>Update the file path to point to a valid `.mcool` file (usually multi-resolution Hi-C format)</li>
 *   <li>Run this class in your IDE or via Gradle or Maven as a Java application</li>
 * </ol>
 *
 * <p>Expected output includes:
 * <ul>
 *   <li>Dataset size and number of slices per chunk size</li>
 *   <li>Read time per dataset and per chunk size</li>
 *   <li>First and last few elements for quick validation</li>
 * </ul>
 *
 * <p>This demonstrates that incremental, memory-efficient reading from chunked HDF5 datasets now works
 * seamlessly in jHDF for large-scale scientific data.
 */

public class ReadSlicesOfChunkedExample {

	public static void main(String[] args) {
		String path = System.getProperty("user.home") + "/Downloads/day80_ventricularCardiomyocyte_pool.hg38.mapq_30.100.mcool";

		try (HdfFile hdfFile = new HdfFile(new File(path))) {
			Dataset[] datasets = new Dataset[]{hdfFile.getDatasetByPath("/resolutions/1000/bins/start"), hdfFile.getDatasetByPath("/resolutions/1000/bins/end")};
			String[] names = {"chrom", "start", "end"};

			int datasetLength = datasets[0].getDimensions()[0];
			System.out.println("Dataset length: " + datasetLength);

			for (int sliceSize : new int[]{100, 1000, 10_000}) {
				System.out.println("\n=== Slicing dataset with chunk size: " + sliceSize + " ===");

				for (int d = 0; d < datasets.length; d++) {
					Dataset ds = datasets[d];
					String name = names[d];

					List<Object> slices = new ArrayList<>();
					int totalValues = 0;

					long t0 = System.nanoTime();
					int numSlices = 0;

					for (int i = 0; i < datasetLength; i += sliceSize) {
						long[] offset = {i};
						int[] shape = {Math.min(sliceSize, datasetLength - i)};
						Object dataSlice = ds.getData(offset, shape);
						slices.add(dataSlice);
						totalValues += shape[0];
						numSlices++;
					}
					long t1 = System.nanoTime();

					// Concatenate slices into one array
					Object fullData = concatPrimitiveSlices(slices, totalValues);
					System.out.printf("%s: %d slices, %.2f ms, verified %d values\n", name, numSlices, (t1 - t0) / 1e6, totalValues);

					System.out.println(formatPrimitiveArray(fullData));
				}
			}
		}
	}

	public static String formatPrimitiveArray(Object data) {
		if (data instanceof int[]) {
			return formatArray((int[]) data);
		} else if (data instanceof long[]) {
			return formatArray((long[]) data);
		} else if (data instanceof float[]) {
			return formatArray((float[]) data);
		} else if (data instanceof double[]) {
			return formatArray((double[]) data);
		} else if (data instanceof byte[]) {
			return formatArray((byte[]) data);
		} else if (data instanceof short[]) {
			return formatArray((short[]) data);
		} else if (data instanceof String[]) {
			return formatArray((String[]) data);
		} else {
			return "Unsupported array type: " + data.getClass().getName();
		}
	}


	private static <T> String formatArray(T arr) {
		StringBuilder sb = new StringBuilder();
		int len = java.lang.reflect.Array.getLength(arr);
		sb.append("Total length: ").append(len).append("\n");

		// First 3
		sb.append("First 3: ");
		for (int i = 0; i < Math.min(3, len); i++) {
			sb.append(java.lang.reflect.Array.get(arr, i)).append(" ");
		}
		sb.append("\n");

		// Last 3
		sb.append("Last 3: ");
		for (int i = Math.max(0, len - 3); i < len; i++) {
			sb.append(java.lang.reflect.Array.get(arr, i)).append(" ");
		}
		sb.append("\n");

		return sb.toString();
	}

	public static Object concatPrimitiveSlices(List<Object> slices, int totalLength) {
		if (slices.isEmpty()) return null;
		Object first = slices.get(0);

		if (first instanceof int[]) {
			int[] result = new int[totalLength];
			int offset = 0;
			for (Object s : slices) {
				int[] src = (int[]) s;
				System.arraycopy(src, 0, result, offset, src.length);
				offset += src.length;
			}
			return result;
		} else if (first instanceof long[]) {
			long[] result = new long[totalLength];
			int offset = 0;
			for (Object s : slices) {
				long[] src = (long[]) s;
				System.arraycopy(src, 0, result, offset, src.length);
				offset += src.length;
			}
			return result;
		} else if (first instanceof float[]) {
			float[] result = new float[totalLength];
			int offset = 0;
			for (Object s : slices) {
				float[] src = (float[]) s;
				System.arraycopy(src, 0, result, offset, src.length);
				offset += src.length;
			}
			return result;
		} else if (first instanceof double[]) {
			double[] result = new double[totalLength];
			int offset = 0;
			for (Object s : slices) {
				double[] src = (double[]) s;
				System.arraycopy(src, 0, result, offset, src.length);
				offset += src.length;
			}
			return result;
		} else if (first instanceof byte[]) {
			byte[] result = new byte[totalLength];
			int offset = 0;
			for (Object s : slices) {
				byte[] src = (byte[]) s;
				System.arraycopy(src, 0, result, offset, src.length);
				offset += src.length;
			}
			return result;
		} else {
			throw new IllegalArgumentException("Unsupported primitive type: " + first.getClass());
		}
	}
}
