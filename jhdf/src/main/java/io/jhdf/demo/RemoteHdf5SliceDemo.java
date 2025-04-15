package io.jhdf.demo;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

import java.net.URI;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

public class RemoteHdf5SliceDemo {

	public static void main(String[] args) throws Exception {
		URI uri = new URI("https://users.abdenlab.org/reimonnt/Cardiac_HiC_PRJNA480492/distiller-nf/results/coolers_library_group/day80_ventricularCardiomyocyte_pool.hg38.mapq_30.100.mcool");
		URL url = uri.toURL();

		System.out.println("Opening remote HDF5 via HTTP range...");
		long t0 = System.nanoTime();
		try (SeekableByteChannel remoteChannel = new HttpRangeSeekableByteChannel(url);
			 HdfFile hdf = new HdfFile(remoteChannel, uri)) {

			String datasetPath = "/resolutions/1000/bins/start";
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
					System.out.println("First 5 values: " + Arrays.toString(Arrays.copyOf((int[]) slice, 5)));
				}
			}
			long t2 = System.nanoTime();
			System.out.printf("Loaded chunked dataset in %d slices of %d. Total values read: %d [%.3f ms]\n", numSlices, sliceSize, totalRead, (t2 - t1) / 1e6);
		}
	}
}
