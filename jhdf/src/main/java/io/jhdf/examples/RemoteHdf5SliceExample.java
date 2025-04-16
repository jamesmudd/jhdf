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
import io.jhdf.storage.HttpRangeSeekableByteChannel;

import java.net.URI;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * Demonstration of reading remote HDF5 files over HTTP using {@link HttpRangeSeekableByteChannel}
 * and the new {@link HdfFile} constructor that supports custom {@link java.nio.channels.SeekableByteChannel}.
 *
 * <p>This example opens a publicly hosted `.hdf5/.mcool` file representing Hi-C data from
 * a remote URL, without downloading the entire file. It uses HTTP range requests to read
 * only the necessary bytes on demand.
 *
 * <p>The demo:
 * <ul>
 *   <li>Initializes a {@code HttpRangeSeekableByteChannel} with LRU caching for remote reads</li>
 *   <li>Opens the HDF5 file using {@code new HdfFile(channel, URI)}</li>
 *   <li>Loads the dataset at {@code /resolutions/1000/bins/start}</li>
 *   <li>Reads the dataset in slices of 1000 rows using {@code getData(offset, shape)}</li>
 *   <li>Prints the shape, the first few values, and the total values read</li>
 * </ul>
 *
 * <p>This demonstrates and validates support for:
 * <ul>
 *   <li>Remote access to chunked HDF5 datasets via HTTP</li>
 *   <li>Streaming partial data without needing to download the full file</li>
 *   <li>Chunked and sliced data access in a real-world genomic `.mcool` file</li>
 * </ul>
 *
 * <p>This is useful for efficiently working with large Hi-C or genomics datasets hosted
 * on cloud or institutional storage without requiring full local copies.
 */

public class RemoteHdf5SliceExample {

	public static void main(String[] args) throws Exception {
		URI uri = new URI("https://users.abdenlab.org/reimonnt/Cardiac_HiC_PRJNA480492/distiller-nf/results/coolers_library_group/day80_ventricularCardiomyocyte_pool.hg38.mapq_30.100.mcool");
		URL url = uri.toURL();

		System.out.println("Opening remote HDF5 via HTTP range: " + url);
		long t0 = System.nanoTime();
		try (SeekableByteChannel remoteChannel = new HttpRangeSeekableByteChannel(url); HdfFile hdf = new HdfFile(remoteChannel, uri)) {

			String datasetPath = "/resolutions/5000/pixels/bin1_id";
			Dataset ds = hdf.getDatasetByPath(datasetPath);

			long t1 = System.nanoTime();
			System.out.printf("Opened dataset: %s [%.3f ms]\n", datasetPath, (t1 - t0) / 1e6);
			System.out.println("Dimensions: " + Arrays.toString(ds.getDimensions()));

			int[] dims = ds.getDimensions();
			int totalLength = dims[0];
			int sliceSize = 1000;
			int numSlices = (int) Math.ceil((double) totalLength / sliceSize);

			int totalRead = 0;

			for (int i = 0; i < totalLength; i += sliceSize) {
				long[] offset = {i};
				int[] shape = {Math.min(sliceSize, totalLength - i)};
				Object slice = ds.getData(offset, shape);
				totalRead += shape[0];

				// Print first slice start
				if (i == 0) {
					System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf((long[]) slice, 5)));
				}
			}
			long t2 = System.nanoTime();
			System.out.printf("Loaded chunked dataset in %d slices of %d. Total values read: %d [%.3f ms]\n", numSlices, sliceSize, totalRead, (t2 - t1) / 1e6);
		}
	}
}
