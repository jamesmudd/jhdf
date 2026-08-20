/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.DatasetCreationOptions;
import io.jhdf.api.StreamingDataset;
import io.jhdf.api.WritableGroup;
import io.jhdf.exceptions.HdfWritingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Writing a chunked dataset a chunk at a time as the data arrives, rather than from an array already in memory.
 */
class StreamingDatasetWritingTest {

	private static double[][][] doubleData3d(int i, int j, int k) {
		double[][][] data = new double[i][j][k];
		for (int a = 0; a < i; a++) {
			for (int b = 0; b < j; b++) {
				for (int c = 0; c < k; c++) {
					data[a][b][c] = a * 100.0 + b * 10.0 + c + 0.5;
				}
			}
		}
		return data;
	}

	private static double[][][] chunkOf(double[][][] data, long[] offset, int[] chunkDimensions) {
		double[][][] chunk = new double[chunkDimensions[0]][chunkDimensions[1]][chunkDimensions[2]];
		for (int a = 0; a < chunkDimensions[0]; a++) {
			for (int b = 0; b < chunkDimensions[1]; b++) {
				for (int c = 0; c < chunkDimensions[2]; c++) {
					int i = Math.toIntExact(offset[0]) + a;
					int j = Math.toIntExact(offset[1]) + b;
					int k = Math.toIntExact(offset[2]) + c;
					if (i < data.length && j < data[0].length && k < data[0][0].length) {
						chunk[a][b][c] = data[i][j][k];
					}
				}
			}
		}
		return chunk;
	}

	@Test
	void streamedChunksReadBackCorrectly() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5};
		Path file = Files.createTempFile("streamed", ".hdf5");

		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			try (StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
				new int[]{6, 8, 10}, DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build())) {
				for (int i = 0; i < 6; i += 2) {
					for (int j = 0; j < 8; j += 4) {
						for (int k = 0; k < 10; k += 5) {
							long[] offset = {i, j, k};
							dataset.writeChunk(offset, chunkOf(data, offset, chunkDimensions));
						}
					}
				}
			}
		}

		try (HdfFile hdfFile = new HdfFile(file)) {
			Dataset dataset = hdfFile.getDatasetByPath("values");
			assertThat(dataset.getDimensions(), is(new int[]{6, 8, 10}));
			assertThat(dataset.getData(), is(data));
		}
	}

	/** Chunks arriving out of order is the normal case for streamed data, not an edge case. */
	@Test
	void chunksMayArriveInAnyOrder() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5};
		Path file = Files.createTempFile("outOfOrder", ".hdf5");

		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			try (StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
				new int[]{6, 8, 10}, DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build())) {
				// Fastest dimension outermost, so chunk indices are supplied back to front
				for (int k = 5; k >= 0; k -= 5) {
					for (int j = 4; j >= 0; j -= 4) {
						for (int i = 4; i >= 0; i -= 2) {
							long[] offset = {i, j, k};
							dataset.writeChunk(offset, chunkOf(data, offset, chunkDimensions));
						}
					}
				}
			}
		}

		try (HdfFile hdfFile = new HdfFile(file)) {
			assertThat(hdfFile.getDatasetByPath("values").getData(), is(data));
		}
	}

	@Test
	void streamedFilteredChunksReadBackCorrectly() throws Exception {
		double[][][] data = doubleData3d(7, 9, 11); // partial edge chunks in every dimension
		int[] chunkDimensions = {2, 4, 5};
		Path file = Files.createTempFile("streamedFiltered", ".hdf5");

		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			try (StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
				new int[]{7, 9, 11},
				DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).shuffle().deflate(4).build())) {
				for (int i = 0; i < 7; i += 2) {
					for (int j = 0; j < 9; j += 4) {
						for (int k = 0; k < 11; k += 5) {
							long[] offset = {i, j, k};
							dataset.writeChunk(offset, chunkOf(data, offset, chunkDimensions));
						}
					}
				}
			}
		}

		try (HdfFile hdfFile = new HdfFile(file)) {
			assertThat(hdfFile.getDatasetByPath("values").getData(), is(data));
		}
	}

	/** Streaming moves the tree, so a file that streams nothing must still be laid out exactly as before. */
	@Test
	void aFileWithNoStreamingIsUnchanged() throws Exception {
		double[][][] data = doubleData3d(4, 4, 4);
		Path before = Files.createTempFile("plain1", ".hdf5");
		Path after = Files.createTempFile("plain2", ".hdf5");
		for (Path file : Arrays.asList(before, after)) {
			try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
				writableHdfFile.putDataset("values", data,
					DatasetCreationOptions.builder().chunkDimensions(2, 2, 2).build());
			}
		}
		assertArrayEquals(Files.readAllBytes(before), Files.readAllBytes(after));

		// The root group is still immediately after the superblock
		try (HdfFile hdfFile = new HdfFile(before)) {
			assertThat(hdfFile.getAddress(), is(64L));
		}
	}

	@Test
	void aChunkThatIsNeverWrittenFailsTheClose() throws Exception {
		Path file = Files.createTempFile("incomplete", ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(file);
		StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
			new int[]{4, 4}, DatasetCreationOptions.builder().chunkDimensions(2, 2).build());
		dataset.writeChunk(new long[]{0, 0}, new double[2][2]);

		HdfWritingException exception = assertThrows(HdfWritingException.class, dataset::close);
		assertThat(exception.getMessage(), containsString("No data was written for the chunk at offset [0, 2]"));

		// The dataset cannot be completed, so the file cannot be either -- better than a file whose dataset
		// reads back as zeros where the missing chunks were
		assertThrows(HdfWritingException.class, writableHdfFile::close);
		Files.deleteIfExists(file);
	}

	@Test
	void aDatasetLeftStreamingFailsTheFileClose() throws Exception {
		Path file = Files.createTempFile("unclosed", ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(file);
		StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
			new int[]{2, 2}, DatasetCreationOptions.builder().chunkDimensions(2, 2).build());
		dataset.writeChunk(new long[]{0, 0}, new double[2][2]);

		HdfWritingException exception = assertThrows(HdfWritingException.class, writableHdfFile::close);
		assertThat(exception.getMessage(), containsString("was still streaming when the file was closed"));
	}

	@Test
	void writingTheSameChunkTwiceIsRejected() throws Exception {
		Path file = Files.createTempFile("twice", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
				new int[]{2, 2}, DatasetCreationOptions.builder().chunkDimensions(2, 2).build());
			dataset.writeChunk(new long[]{0, 0}, new double[2][2]);

			HdfWritingException exception = assertThrows(HdfWritingException.class,
				() -> dataset.writeChunk(new long[]{0, 0}, new double[2][2]));
			assertThat(exception.getMessage(), containsString("has already been written"));
			dataset.close();
		}
	}

	@Test
	void anOffsetThatIsNotAChunkBoundaryIsRejected() throws Exception {
		Path file = Files.createTempFile("badOffset", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
				new int[]{4, 4}, DatasetCreationOptions.builder().chunkDimensions(2, 2).build());

			HdfWritingException exception = assertThrows(HdfWritingException.class,
				() -> dataset.writeChunk(new long[]{1, 0}, new double[2][2]));
			assertThat(exception.getMessage(), containsString("is not the start of a chunk"));

			for (int i = 0; i < 4; i += 2) {
				for (int j = 0; j < 4; j += 2) {
					dataset.writeChunk(new long[]{i, j}, new double[2][2]);
				}
			}
			dataset.close();
		}
	}

	/** VCell writes its streamed datasets two groups deep, which is the normal case, not the root. */
	@Test
	void streamsIntoANestedGroup() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5};
		Path file = Files.createTempFile("nested", ".hdf5");

		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			WritableGroup variable = writableHdfFile.putGroup("SimID_1").putGroup("Ran_cyt");
			try (StreamingDataset dataset = variable.newStreamingDataset("DataValues (XYT)", double.class,
				new int[]{6, 8, 10}, DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build())) {
				for (int i = 0; i < 6; i += 2) {
					for (int j = 0; j < 8; j += 4) {
						for (int k = 0; k < 10; k += 5) {
							long[] offset = {i, j, k};
							dataset.writeChunk(offset, chunkOf(data, offset, chunkDimensions));
						}
					}
				}
			}
		}

		try (HdfFile hdfFile = new HdfFile(file)) {
			assertThat(hdfFile.getDatasetByPath("SimID_1/Ran_cyt/DataValues (XYT)").getData(), is(data));
		}
	}

	@Test
	void aGroupOutsideAFileCannotStream() {
		WritableGroup detached = new io.jhdf.WritableGroupImpl(null, "detached");
		HdfWritingException exception = assertThrows(HdfWritingException.class, () ->
			detached.newStreamingDataset("values", double.class, new int[]{2, 2},
				DatasetCreationOptions.builder().chunkDimensions(2, 2).build()));
		assertThat(exception.getMessage(), containsString("nowhere to stream chunks to"));
	}

	/**
	 * The point of streaming: a dataset several times the heap, written as the data arrives. Opt in with
	 * {@code -Djhdf.test.largeWrite=true} because it is slow and writes a multi gigabyte file.
	 */
	@Test
	@EnabledIfSystemProperty(named = "jhdf.test.largeWrite", matches = "true")
	void streamsADatasetLargerThanTheHeap() throws Exception {
		Path file = Files.createTempFile("streamedLarge", ".hdf5");
		try {
			int[] dimensions = {200, 200, 50, 200}; // 3.2 GB of doubles, a VCell export
			try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
				try (StreamingDataset dataset = writableHdfFile.newStreamingDataset("values", double.class,
					dimensions, DatasetCreationOptions.builder().chunkDimensions(200, 200, 1, 1).build())) {
					// Time major, exactly how the data is produced, and not the chunk index order
					for (int t = 0; t < 200; t++) {
						for (int slice = 0; slice < 50; slice++) {
							double[][][][] chunk = new double[200][200][1][1];
							double value = slice * 1000.0 + t;
							for (int x = 0; x < 200; x++) {
								for (int y = 0; y < 200; y++) {
									chunk[x][y][0][0] = value;
								}
							}
							dataset.writeChunk(new long[]{0, 0, slice, t}, chunk);
						}
					}
				}
			}

			assertThat(Files.size(file) > 3_000_000_000L, is(true));

			try (HdfFile hdfFile = new HdfFile(file)) {
				double[][][][] slice = (double[][][][]) hdfFile.getDatasetByPath("values")
					.getData(new long[]{0, 0, 17, 42}, new int[]{200, 200, 1, 1});
				assertThat(slice[0][0][0][0], is(17_042.0));
				assertThat(slice[199][199][0][0], is(17_042.0));
			}
		} finally {
			Files.deleteIfExists(file);
		}
	}
}
