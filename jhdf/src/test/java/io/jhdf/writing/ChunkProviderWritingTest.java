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
import io.jhdf.WritableDatasetImpl;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.ChunkProvider;
import io.jhdf.api.Dataset;
import io.jhdf.api.DatasetCreationOptions;
import io.jhdf.exceptions.HdfWritingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Writing a chunked dataset from a {@link ChunkProvider}, so the whole dataset is never in memory.
 */
class ChunkProviderWritingTest {

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

	/** Slices chunks out of an in memory dataset, standing in for an application reading them from elsewhere. */
	private static ChunkProvider sliceOf(double[][][] data, int[] chunkDimensions) {
		return chunkOffset -> {
			double[][][] chunk = new double[chunkDimensions[0]][chunkDimensions[1]][chunkDimensions[2]];
			for (int a = 0; a < chunkDimensions[0]; a++) {
				for (int b = 0; b < chunkDimensions[1]; b++) {
					for (int c = 0; c < chunkDimensions[2]; c++) {
						int i = Math.toIntExact(chunkOffset[0]) + a;
						int j = Math.toIntExact(chunkOffset[1]) + b;
						int k = Math.toIntExact(chunkOffset[2]) + c;
						// Chunks overhanging the dataset keep the zero padding
						if (i < data.length && j < data[0].length && k < data[0][0].length) {
							chunk[a][b][c] = data[i][j][k];
						}
					}
				}
			}
			return chunk;
		};
	}

	private Path writeWithProvider(double[][][] data, int[] chunkDimensions, DatasetCreationOptions options)
		throws IOException {
		Path file = Files.createTempFile("provider", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			writableHdfFile.putDataset("data", double.class,
				new int[]{data.length, data[0].length, data[0][0].length}, options, sliceOf(data, chunkDimensions));
		}
		return file;
	}

	private Path writeInMemory(double[][][] data, DatasetCreationOptions options) throws IOException {
		Path file = Files.createTempFile("inMemory", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			writableHdfFile.putDataset("data", data, options);
		}
		return file;
	}

	/**
	 * The strongest statement available that supplying chunks changes nothing: for a dataset small enough to write
	 * both ways, the two files are identical.
	 */
	@Test
	void providedChunksProduceTheSameFileAsWritingTheWholeDataset() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5}; // divides the dataset exactly
		DatasetCreationOptions options = DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build();

		assertArrayEquals(
			Files.readAllBytes(writeInMemory(data, options)),
			Files.readAllBytes(writeWithProvider(data, chunkDimensions, options)));
	}

	@Test
	void providedChunksMatchWithPartialEdgeChunks() throws Exception {
		double[][][] data = doubleData3d(7, 9, 11);
		int[] chunkDimensions = {2, 4, 5}; // overhangs the dataset in every dimension
		DatasetCreationOptions options = DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build();

		assertArrayEquals(
			Files.readAllBytes(writeInMemory(data, options)),
			Files.readAllBytes(writeWithProvider(data, chunkDimensions, options)));
	}

	@Test
	void providedChunksMatchWithFilters() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5};
		DatasetCreationOptions options = DatasetCreationOptions.builder()
			.chunkDimensions(chunkDimensions).shuffle().deflate(4).build();

		assertArrayEquals(
			Files.readAllBytes(writeInMemory(data, options)),
			Files.readAllBytes(writeWithProvider(data, chunkDimensions, options)));
	}

	@Test
	void providedDataReadsBackCorrectly() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5};
		Path file = writeWithProvider(data, chunkDimensions,
			DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).deflate(4).build());

		try (HdfFile hdfFile = new HdfFile(file)) {
			Dataset dataset = hdfFile.getDatasetByPath("data");
			assertThat(dataset.getDimensions(), is(new int[]{6, 8, 10}));
			assertThat(dataset.getData(), is(data));
		}
	}

	/** Every chunk exactly once, so a provider can be a cursor over an external source without bookkeeping. */
	@Test
	void everyChunkIsRequestedExactlyOnce() throws Exception {
		double[][][] data = doubleData3d(6, 8, 10);
		int[] chunkDimensions = {2, 4, 5};
		ChunkProvider delegate = sliceOf(data, chunkDimensions);
		List<String> requested = new ArrayList<>();

		Path file = Files.createTempFile("counted", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			writableHdfFile.putDataset("data", double.class, new int[]{6, 8, 10},
				DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build(),
				chunkOffset -> {
					requested.add(Arrays.toString(chunkOffset));
					return delegate.getChunk(chunkOffset);
				});
		}

		Set<String> unique = new HashSet<>(requested);
		assertThat(requested.size(), is(3 * 2 * 2)); // 6/2 * 8/4 * 10/5
		assertThat(unique.size(), is(requested.size()));
	}

	@Test
	void aChunkThatIsNotSuppliedFailsTheWrite() throws Exception {
		Path file = Files.createTempFile("missing", ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(file);
		writableHdfFile.putDataset("data", double.class, new int[]{4, 4},
			DatasetCreationOptions.builder().chunkDimensions(2, 2).build(),
			chunkOffset -> chunkOffset[0] == 0 ? new double[2][2] : null);

		HdfWritingException exception = assertThrows(HdfWritingException.class, writableHdfFile::close);
		assertThat(exception.getMessage(), containsString("No data supplied for the chunk at offset"));
	}

	@Test
	void aWrongShapedChunkFailsTheWrite() throws Exception {
		Path file = Files.createTempFile("wrongShape", ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(file);
		writableHdfFile.putDataset("data", double.class, new int[]{4, 4},
			DatasetCreationOptions.builder().chunkDimensions(2, 2).build(),
			chunkOffset -> new double[3][3]);

		HdfWritingException exception = assertThrows(HdfWritingException.class, writableHdfFile::close);
		assertThat(exception.getMessage(), containsString("expected [32] bytes for chunk dimensions [2, 2]"));
	}

	@Test
	void chunkDimensionsAreRequired() throws Exception {
		Path file = Files.createTempFile("noChunks", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			HdfWritingException exception = assertThrows(HdfWritingException.class, () ->
				writableHdfFile.putDataset("data", double.class, new int[]{4, 4},
					DatasetCreationOptions.DEFAULT, chunkOffset -> new double[2][2]));
			assertThat(exception.getMessage(), containsString("Chunk dimensions must be specified"));
			writableHdfFile.putDataset("placeholder", new int[]{1});
		}
	}

	/**
	 * The point of the feature: a dataset too large to hold in memory can still be declared. Writing 2 GB of it
	 * would make this test unreasonably slow, so this asserts only that the limit which exists purely because the
	 * in memory path buffers everything no longer applies.
	 */
	@Test
	void aDatasetLargerThanTheInMemoryLimitCanBeDeclared() throws Exception {
		Path file = Files.createTempFile("large", ".hdf5");
		try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
			int[] dimensions = {1024, 1024, 512}; // 4 GiB of doubles, four times the in memory limit

			// Constructed rather than put into the file, so closing does not write 4 GiB. Writing a dataset that
			// size for real is covered by writesADatasetLargerThanTheHeap.
			Dataset declared = new WritableDatasetImpl(double.class, dimensions, "data", writableHdfFile,
				DatasetCreationOptions.builder().chunkDimensions(1024, 1024, 1).build(),
				chunkOffset -> new double[1024][1024]);

			assertThat(declared.getSizeInBytes(), is(4L * 1024 * 1024 * 1024));

			// The in memory path's limit is not asserted here: tripping it needs an array larger than
			// Integer.MAX_VALUE bytes, which cannot be allocated, which is the reason the limit exists.

			writableHdfFile.putDataset("placeholder", new int[]{1});
		}
		Files.deleteIfExists(file);
	}

	/**
	 * Actually writes a dataset several times the size of the test JVM's heap, which is the only test that proves
	 * the feature does what it claims. Opt in with {@code -Djhdf.test.largeWrite=true} because it is slow and
	 * writes a multi gigabyte file.
	 */
	@Test
	@EnabledIfSystemProperty(named = "jhdf.test.largeWrite", matches = "true")
	void writesADatasetLargerThanTheHeap() throws Exception {
		Path file = Files.createTempFile("largeWrite", ".hdf5");
		try {
			// 200x200 grid, 50 slices, 200 timepoints of doubles = 3.2 GB, VCell's export shape
			int[] dimensions = {200, 200, 50, 200};
			int[] chunkDimensions = {200, 200, 1, 1};

			try (WritableHdfFile writableHdfFile = HdfFile.write(file)) {
				writableHdfFile.putDataset("values", double.class, dimensions,
					DatasetCreationOptions.builder().chunkDimensions(chunkDimensions).build(),
					chunkOffset -> {
						double[][][][] chunk = new double[200][200][1][1];
						double value = chunkOffset[2] * 1000.0 + chunkOffset[3];
						for (int x = 0; x < 200; x++) {
							for (int y = 0; y < 200; y++) {
								chunk[x][y][0][0] = value;
							}
						}
						return chunk;
					});
			}

			assertThat(Files.size(file) > 3_000_000_000L, is(true));

			try (HdfFile hdfFile = new HdfFile(file)) {
				Dataset dataset = hdfFile.getDatasetByPath("values");
				assertThat(dataset.getDimensions(), is(dimensions));
				// Spot check a slice rather than reading 3.2 GB back
				double[][][][] slice =
					(double[][][][]) dataset.getData(new long[]{0, 0, 17, 42}, new int[]{200, 200, 1, 1});
				assertThat(slice[0][0][0][0], is(17_042.0));
				assertThat(slice[199][199][0][0], is(17_042.0));
			}
		} finally {
			Files.deleteIfExists(file);
		}
	}
}
