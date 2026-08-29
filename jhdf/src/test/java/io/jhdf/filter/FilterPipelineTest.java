/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FilterPipelineTest {

	@Test
	void decodeSkipsMaskedFilterUsingOriginalPipelinePosition() {
		FilterPipeline pipeline = new FilterPipeline();
		pipeline.addFilter(new AppendingFilter(0), new int[0]);
		pipeline.addFilter(new AppendingFilter(1), new int[0]);

		BitSet filterMask = new BitSet();
		filterMask.set(0);

		assertArrayEquals(new byte[]{1}, pipeline.decode(new byte[0], filterMask));
	}

	@Test
	void decodeWithoutMaskAppliesEveryFilterInReverseOrder() {
		FilterPipeline pipeline = new FilterPipeline();
		pipeline.addFilter(new AppendingFilter(0), new int[0]);
		pipeline.addFilter(new AppendingFilter(1), new int[0]);

		assertArrayEquals(new byte[]{1, 0}, pipeline.decode(new byte[0]));
	}

	private static final class AppendingFilter implements Filter {

		private final int id;

		private AppendingFilter(int id) {
			this.id = id;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getName() {
			return "filter-" + id;
		}

		@Override
		public byte[] decode(byte[] encodedData, int[] filterData) {
			byte[] decodedData = Arrays.copyOf(encodedData, encodedData.length + 1);
			decodedData[decodedData.length - 1] = (byte) id;
			return decodedData;
		}
	}
}
