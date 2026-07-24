/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.TestUtils;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.DatasetCreationOptions;
import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.dataset.ChunkedDataset;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.exceptions.HdfWritingException;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import io.jhdf.object.message.DataLayout;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChunkedDatasetWritingTest {

	// Deterministic test data generators

	private static int[] intData1d(int length) {
		int[] data = new int[length];
		for (int i = 0; i < length; i++) {
			data[i] = i * i - 3 * i + 7;
		}
		return data;
	}

	private static byte[][] byteData2d(int rows, int columns) {
		byte[][] data = new byte[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = (byte) (i * 31 + j * 7);
			}
		}
		return data;
	}

	private static short[][] shortData2d(int rows, int columns) {
		short[][] data = new short[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = (short) (i * 313 - j * 11);
			}
		}
		return data;
	}

	private static long[] longData1d(int length) {
		long[] data = new long[length];
		for (int i = 0; i < length; i++) {
			data[i] = 987654321L * i - 12345L;
		}
		return data;
	}

	private static double[][] doubleData2d(int rows, int columns) {
		double[][] data = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = Math.sin(i * 0.1) * 1000 + j * 0.25;
			}
		}
		return data;
	}

	private static float[][][] floatData3d(int d0, int d1, int d2) {
		float[][][] data = new float[d0][d1][d2];
		for (int i = 0; i < d0; i++) {
			for (int j = 0; j < d1; j++) {
				for (int k = 0; k < d2; k++) {
					data[i][j][k] = i * 100.5f - j * 10.25f + k * 0.125f;
				}
			}
		}
		return data;
	}

	private static int[][] intData2d(int rows, int columns) {
		int[][] data = new int[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				data[i][j] = i * 1000 + j;
			}
		}
		return data;
	}

	private static int[][][][] intData4d(int d0, int d1, int d2, int d3) {
		int[][][][] data = new int[d0][d1][d2][d3];
		for (int i = 0; i < d0; i++) {
			for (int j = 0; j < d1; j++) {
				for (int k = 0; k < d2; k++) {
					for (int l = 0; l < d3; l++) {
						data[i][j][k][l] = i * 1000 + j * 100 + k * 10 + l;
					}
				}
			}
		}
		return data;
	}

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ChunkedDatasets {
		private Path tempFile;

		private final int[] int1d = intData1d(100);
		private final double[][] double2d = doubleData2d(47, 13);
		private final float[][][] float3d = floatData3d(5, 7, 11);
		private final byte[][] byte2d = byteData2d(20, 20);
		private final short[][] short2d = shortData2d(40, 40);
		private final long[] long1d = longData1d(50);
		private final int[][] int2d = intData2d(70, 70);
		private final int[][][][] int4d = intData4d(3, 4, 5, 7);
		private final float[][][] float3dCompressible = floatData3d(6, 8, 10);
		private final String[] string1d = new String[]{"one", "two", "three", "four", "five", "six", "seven"};

		@Test
		@Order(1)
		void writeChunkedDatasets() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

			// Chunked without filters, has a partial edge chunk 100/7
			writableHdfFile.putDataset("int1d_chunked", int1d, DatasetCreationOptions.builder()
				.chunkDimensions(7)
				.build());

			// Chunked with deflate
			writableHdfFile.putDataset("int1d_deflate", int1d, DatasetCreationOptions.builder()
				.chunkDimensions(7)
				.deflate(4)
				.build());

			// Chunked with shuffle only
			writableHdfFile.putDataset("int1d_shuffle", int1d, DatasetCreationOptions.builder()
				.chunkDimensions(9)
				.shuffle()
				.build());

			// 2D with shuffle + deflate, partial chunks in both dimensions
			writableHdfFile.putDataset("double2d_shuffle_deflate", double2d, DatasetCreationOptions.builder()
				.chunkDimensions(10, 5)
				.shuffle()
				.deflate(6)
				.build());

			// 3D with deflate, partial chunks in every dimension
			writableHdfFile.putDataset("float3d_deflate", float3d, DatasetCreationOptions.builder()
				.chunkDimensions(2, 3, 4)
				.deflate()
				.build());

			// Single byte element size with max compression
			writableHdfFile.putDataset("byte2d_deflate", byte2d, DatasetCreationOptions.builder()
				.chunkDimensions(8, 8)
				.deflate(9)
				.build());

			// Chunk dimensions equal dataset dimensions so a filtered single chunk
			writableHdfFile.putDataset("long1d_single_chunk_filtered", long1d, DatasetCreationOptions.builder()
				.chunkDimensions(50)
				.shuffle()
				.deflate(6)
				.build());

			// Single chunk without filters
			writableHdfFile.putDataset("int1d_single_chunk", int1d, DatasetCreationOptions.builder()
				.chunkDimensions(100)
				.build());

			// Filters without chunk dimensions, written as a single chunk covering the dataset
			writableHdfFile.putDataset("short2d_whole_deflate", short2d, DatasetCreationOptions.builder()
				.deflate()
				.build());

			// Dataset dimensions an exact multiple of the chunk dimensions
			writableHdfFile.putDataset("int1d_exact_chunks", intData1d(20), DatasetCreationOptions.builder()
				.chunkDimensions(5)
				.build());

			// 2D chunk grid 24x24=576 chunks with partial edge chunks in both dimensions
			writableHdfFile.putDataset("int2d_many_chunks", int2d, DatasetCreationOptions.builder()
				.chunkDimensions(3, 3)
				.shuffle()
				.deflate(2)
				.build());

			// 4D with deflate, partial chunks in every dimension
			writableHdfFile.putDataset("int4d_deflate", int4d, DatasetCreationOptions.builder()
				.chunkDimensions(2, 2, 3, 3)
				.deflate(4)
				.build());

			// 4D with shuffle + deflate
			writableHdfFile.putDataset("int4d_shuffle_deflate", int4d, DatasetCreationOptions.builder()
				.chunkDimensions(2, 2, 3, 3)
				.shuffle()
				.deflate(6)
				.build());

			// 4D dataset dimensions exact multiple of chunk dimensions
			writableHdfFile.putDataset("int4d_exact_chunks", intData4d(4, 6, 6, 9), DatasetCreationOptions.builder()
				.chunkDimensions(2, 3, 3, 3)
				.shuffle()
				.deflate(5)
				.build());

			// 3D with shuffle + deflate (complements the existing float3d_deflate test)
			writableHdfFile.putDataset("float3d_shuffle_deflate", float3dCompressible, DatasetCreationOptions.builder()
				.chunkDimensions(3, 4, 5)
				.shuffle()
				.deflate(7)
				.build());

			// Fixed length strings chunked and compressed
			writableHdfFile.putDataset("string1d_deflate", string1d, DatasetCreationOptions.builder()
				.chunkDimensions(3)
				.deflate(6)
				.build());

			// In a sub group with an attribute on the dataset. Named so it sorts after the datasets in h5dump
			// output, the XML test helper cannot parse datasets interleaved with groups (Jackson unwrapped lists)
			WritableGroup group = writableHdfFile.putGroup("zz_group");
			WritableDataset datasetWithAttribute = group.putDataset("double2d_in_group", double2d,
				DatasetCreationOptions.builder()
					.chunkDimensions(16, 8)
					.shuffle()
					.deflate(6)
					.build());
			datasetWithAttribute.putAttribute("description", "chunked and compressed");

			// Check the writable dataset reports chunked before writing
			assertThat(datasetWithAttribute.getDataLayout(), is(DataLayout.CHUNKED));
			List<String> writableFilterNames = datasetWithAttribute.getFilters().stream()
				.map(PipelineFilterWithData::getName)
				.toList();
			assertThat(writableFilterNames, contains("shuffle", "deflate"));

			// Actually flush and write everything
			writableHdfFile.close();

			// Now read it back
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				assertDatasetMatches(hdfFile, "int1d_chunked", int1d, new int[]{7}, false);
				assertDatasetMatches(hdfFile, "int1d_deflate", int1d, new int[]{7}, true);
				assertDatasetMatches(hdfFile, "int1d_shuffle", int1d, new int[]{9}, true);
				assertDatasetMatches(hdfFile, "double2d_shuffle_deflate", double2d, new int[]{10, 5}, true);
				assertDatasetMatches(hdfFile, "float3d_deflate", float3d, new int[]{2, 3, 4}, true);
				assertDatasetMatches(hdfFile, "byte2d_deflate", byte2d, new int[]{8, 8}, true);
				assertDatasetMatches(hdfFile, "long1d_single_chunk_filtered", long1d, new int[]{50}, true);
				assertDatasetMatches(hdfFile, "int1d_single_chunk", int1d, new int[]{100}, false);
				assertDatasetMatches(hdfFile, "short2d_whole_deflate", short2d, new int[]{40, 40}, true);
				assertDatasetMatches(hdfFile, "int1d_exact_chunks", intData1d(20), new int[]{5}, false);
				assertDatasetMatches(hdfFile, "int2d_many_chunks", int2d, new int[]{3, 3}, true);
				assertDatasetMatches(hdfFile, "int4d_deflate", int4d, new int[]{2, 2, 3, 3}, true);
				assertDatasetMatches(hdfFile, "int4d_shuffle_deflate", int4d, new int[]{2, 2, 3, 3}, true);
				assertDatasetMatches(hdfFile, "int4d_exact_chunks", intData4d(4, 6, 6, 9), new int[]{2, 3, 3, 3}, true);
				assertDatasetMatches(hdfFile, "float3d_shuffle_deflate", float3dCompressible, new int[]{3, 4, 5}, true);
				assertDatasetMatches(hdfFile, "string1d_deflate", string1d, new int[]{3}, true);
				assertDatasetMatches(hdfFile, "zz_group/double2d_in_group", double2d, new int[]{16, 8}, true);

				// Compression should reduce the storage size of this very compressible dataset
				Dataset compressed = hdfFile.getDatasetByPath("short2d_whole_deflate");
				assertThat(compressed.getStorageInBytes(), is(lessThan(compressed.getSizeInBytes())));

				// The attribute survived
				assertThat(hdfFile.getDatasetByPath("zz_group/double2d_in_group")
					.getAttribute("description").getData(), is(equalTo("chunked and compressed")));

				// Filter details are readable
				Dataset shuffleDeflate = hdfFile.getDatasetByPath("double2d_shuffle_deflate");
				List<PipelineFilterWithData> filters = shuffleDeflate.getFilters();
				assertThat(filters.stream().map(PipelineFilterWithData::getName).toList(),
					contains("shuffle", "deflate"));
				// Shuffle filter data is the element size
				assertThat(filters.get(0).getFilterData(), is(equalTo(new int[]{8})));
				// Deflate filter data is the compression level
				assertThat(filters.get(1).getFilterData(), is(equalTo(new int[]{6})));

				// Filter details for 4D shuffle + deflate dataset
				Dataset int4dShuffleDeflate = hdfFile.getDatasetByPath("int4d_shuffle_deflate");
				List<PipelineFilterWithData> filters4d = int4dShuffleDeflate.getFilters();
				assertThat(filters4d.stream().map(PipelineFilterWithData::getName).toList(),
					contains("shuffle", "deflate"));
				// Shuffle filter data is the element size for int (4 bytes)
				assertThat(filters4d.get(0).getFilterData(), is(equalTo(new int[]{4})));
				// Deflate filter data is the compression level
				assertThat(filters4d.get(1).getFilterData(), is(equalTo(new int[]{6})));

				// Just check the whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
				TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
			}
		}

		private void assertDatasetMatches(HdfFile hdfFile, String path, Object expectedData, int[] expectedChunkDimensions, boolean expectFilters) {
			Dataset dataset = hdfFile.getDatasetByPath(path);
			assertThat("data of " + path, dataset.getData(), is(equalTo(expectedData)));
			assertThat("layout of " + path, dataset.getDataLayout(), is(DataLayout.CHUNKED));
			assertThat(dataset, is(org.hamcrest.Matchers.instanceOf(ChunkedDataset.class)));
			assertThat("chunk dimensions of " + path, ((ChunkedDataset) dataset).getChunkDimensions(),
				is(equalTo(expectedChunkDimensions)));
			assertThat("filters of " + path, dataset.getFilters().size(), expectFilters ? is(greaterThan(0)) : is(0));
		}

		@Test
		@Order(2)
		@EnabledIfH5DumpAvailable
		void readChunkedDatasetsWithH5Dump() throws Exception {
			// Read with h5dump
			HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

			// Read with jhdf
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				// Compare
				H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
			}
		}
	}

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ManyChunks {
		private Path tempFile;

		// 2100 chunks needs a fixed array page size above the default 1024
		private final int[] data = intData1d(4200);

		@Test
		@Order(1)
		void writeManyChunks() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

			writableHdfFile.putDataset("many_chunks_deflate", data, DatasetCreationOptions.builder()
				.chunkDimensions(2)
				.deflate(1)
				.build());
			writableHdfFile.putDataset("many_chunks_plain", data, DatasetCreationOptions.builder()
				.chunkDimensions(2)
				.build());
			writableHdfFile.close();

			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				assertThat(hdfFile.getDatasetByPath("many_chunks_deflate").getData(), is(equalTo(data)));
				assertThat(hdfFile.getDatasetByPath("many_chunks_plain").getData(), is(equalTo(data)));
			}
		}

		@Test
		@Order(2)
		@EnabledIfH5DumpAvailable
		void readManyChunksWithH5Dump() throws Exception {
			HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
			}
		}
	}

	@Nested
	class Validation {

		@Test
		void scalarDatasetCannotBeChunked() throws Exception {
			Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			try (WritableHdfFile writableHdfFile = HdfFile.write(tempFile)) {
				DatasetCreationOptions options = DatasetCreationOptions.builder().deflate().build();
				assertThrows(HdfWritingException.class, () ->
					writableHdfFile.putDataset("scalar", 123, options));
			} finally {
				Files.deleteIfExists(tempFile);
			}
		}

		@Test
		void chunkDimensionsRankMustMatch() throws Exception {
			Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			try (WritableHdfFile writableHdfFile = HdfFile.write(tempFile)) {
				DatasetCreationOptions options = DatasetCreationOptions.builder().chunkDimensions(5, 5).build();
				assertThrows(HdfWritingException.class, () ->
					writableHdfFile.putDataset("1d", new int[10], options));
			} finally {
				Files.deleteIfExists(tempFile);
			}
		}

		@Test
		void chunkDimensionsCannotExceedDatasetDimensions() throws Exception {
			Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			try (WritableHdfFile writableHdfFile = HdfFile.write(tempFile)) {
				DatasetCreationOptions options = DatasetCreationOptions.builder().chunkDimensions(11).build();
				assertThrows(HdfWritingException.class, () ->
					writableHdfFile.putDataset("1d", new int[10], options));
			} finally {
				Files.deleteIfExists(tempFile);
			}
		}

		@Test
		void invalidBuilderArguments() {
			DatasetCreationOptions.Builder builder = DatasetCreationOptions.builder();
			assertThrows(IllegalArgumentException.class, () -> builder.chunkDimensions());
			assertThrows(IllegalArgumentException.class, () -> builder.chunkDimensions(0));
			assertThrows(IllegalArgumentException.class, () -> builder.chunkDimensions(-4));
			assertThrows(IllegalArgumentException.class, () -> builder.deflate(10));
			assertThrows(IllegalArgumentException.class, () -> builder.deflate(-1));
		}

		@Test
		void noChunkingGivesContiguousDataset() throws Exception {
			// Empty options should behave exactly like the putDataset without options
			Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			try {
				WritableHdfFile writableHdfFile = HdfFile.write(tempFile);
				WritableDataset dataset = writableHdfFile.putDataset("contiguous", intData1d(10),
					DatasetCreationOptions.builder().build());
				assertThat(dataset.getDataLayout(), is(DataLayout.CONTIGUOUS));
				writableHdfFile.close();

				try (HdfFile hdfFile = new HdfFile(tempFile)) {
					Dataset readDataset = hdfFile.getDatasetByPath("contiguous");
					assertThat(readDataset.getDataLayout(), is(DataLayout.CONTIGUOUS));
					assertThat(readDataset.getData(), is(equalTo(intData1d(10))));
				}
			} finally {
				Files.deleteIfExists(tempFile);
			}
		}
	}
}
