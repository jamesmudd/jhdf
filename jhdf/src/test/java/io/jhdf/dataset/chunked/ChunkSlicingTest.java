/*
/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.dataset.chunked;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.dataset.ChunkedDataset;
import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.jhdf.Utils.getDimensions;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test slicing for all four 3D int16 “large_int16” datasets in
 * chunked_v4_datasets.hdf5.
 * <p>
 * For each dataset:
 * 1. Load the full 200×5×10 array into memory.
 * 2. Exercise slices along each dimension at a few offsets & lengths.
 * 3. Verify returned shape and data match the corresponding subarray
 * of the in‑memory full array.
 */
class ChunkSlicingTest {

	private static final String TEST_FILE = "/hdf5/chunked_v4_datasets.hdf5";
	private static HdfFile h5;

	// Paths to the four large_int16 datasets
	private static final String[] PATHS = {"/btree_v2/large_int16", "/extensible_array/large_int16", "/filtered_btree_v2/large_int16", "/filtered_extensible_array/large_int16"};

	@BeforeAll
	static void openFile() {
		h5 = HdfFile.fromInputStream(ChunkSlicingTest.class.getResourceAsStream(TEST_FILE));
	}

	@AfterAll
	static void closeFile() {
		h5.close();
	}

	@TestFactory
	Stream<DynamicNode> slicingTests() {
		List<DynamicNode> suites = new ArrayList<>();

		for (String path : PATHS) {
			Dataset ds = h5.getDatasetByPath(path);
			// load full dataset into memory
			short[][][] full = (short[][][]) ds.getData();
			int[] dims = ds.getDimensions(); // [200, 5, 10]

			List<DynamicTest> tests = new ArrayList<>();

			// --- full-read sanity check ---
			tests.add(DynamicTest.dynamicTest("fullRead", () -> {
				// shape
				assertArrayEquals(dims, ds.getDimensions());
				// data
				short[][][] got = (short[][][]) ds.getData();
				for (int i = 0; i < dims[0]; i++)
					for (int j = 0; j < dims[1]; j++)
						for (int k = 0; k < dims[2]; k++)
							assertEquals(full[i][j][k], got[i][j][k], "full[" + i + "][" + j + "][" + k + "]");
			}));

			// offsets & lengths to test along each axis
			int[][] offsets = {{0, 8, 11, 101, 123},  // for dim0
				{0, 1, 2}, // for dim1
				{0, 3, 4, 7}  // for dim2
			};
			int[][] lengths = {{1, 2, 3, 5, 11, 23, 49, 110},  // for dim0
				{1, 3, 4, 5},  // for dim1
				{1, 2, 3, 4}   // for dim2
			};

			// generate slice tests for each dimension
			for (int dim = 0; dim < 3; dim++) {
				for (int off : offsets[dim]) {
					for (int len : lengths[dim]) {
						// skip invalid (runs off end)
						if (off + len > dims[dim]) continue;

						long[] sliceOff = new long[]{0, 0, 0};
						int[] sliceLen = new int[]{dims[0], dims[1], dims[2]};
						sliceOff[dim] = off;
						sliceLen[dim] = len;

						String name = String.format("slice_dim%d_off%d_len%d", dim, off, len);
						tests.add(DynamicTest.dynamicTest(name, () -> {
							Object raw = ds.getData(sliceOff, sliceLen);
							short[][][] slice = (short[][][]) raw;

							// 1) shape matches
							assertArrayEquals(sliceLen, getDimensions(slice), "returned shape");

							// 2) data matches full subarray
							for (int i = 0; i < sliceLen[0]; i++)
								for (int j = 0; j < sliceLen[1]; j++)
									for (int k = 0; k < sliceLen[2]; k++)
										assertEquals(full[(int) sliceOff[0] + i][(int) sliceOff[1] + j][(int) sliceOff[2] + k], slice[i][j][k], String.format("slice[%d][%d][%d]", i, j, k));
						}));
					}
				}
			}

			suites.add(DynamicContainer.dynamicContainer(path, tests));
		}

		return suites.stream();
	}

	@Test
	void invalidChunkOffsetThrows() {
		ChunkedDataset cd = (ChunkedDataset) h5.getDatasetByPath("/fixed_array/int32");
		Assertions.assertThrows(HdfException.class, () -> cd.getRawChunkBuffer(new int[]{2, 2}));
	}
}
