/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api;

import io.jhdf.filter.ByteShuffleFilter;
import io.jhdf.filter.DeflatePipelineFilter;
import io.jhdf.filter.Filter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Options controlling how a dataset is stored when it is written. Obtained via {@link #builder()} e.g.
 *
 * <pre>{@code
 * writableHdfFile.putDataset("compressed", data, DatasetCreationOptions.builder()
 * 		.chunkDimensions(64, 64)
 * 		.shuffle()
 * 		.deflate(4)
 * 		.build());
 * }</pre>
 *
 * <p>Adding any filter (e.g. {@link Builder#deflate(int)}) enables chunked storage. If no chunk dimensions are
 * specified the dataset is written as a single chunk.</p>
 *
 * @author James Mudd
 * @since v0.13.0
 */
public final class DatasetCreationOptions {

	/**
	 * The default options, a contiguous dataset with no filters.
	 */
	public static final DatasetCreationOptions DEFAULT = builder().build();

	private final int[] chunkDimensions;
	private final List<RequestedFilter> filters;
	private final boolean unsigned;

	private DatasetCreationOptions(Builder builder) {
		this.chunkDimensions = ArrayUtils.clone(builder.chunkDimensions);
		this.filters = Collections.unmodifiableList(new ArrayList<>(builder.filters));
		this.unsigned = builder.unsigned;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * @return true if the dataset should use chunked storage
	 */
	public boolean isChunked() {
		return chunkDimensions != null || !filters.isEmpty();
	}

	/**
	 * @return true if the underlying fixed point (integer) data type should be written as unsigned
	 */
	public boolean isUnsigned() {
		return unsigned;
	}

	/**
	 * @return the requested chunk dimensions, or null if not specified
	 */
	public int[] getChunkDimensions() {
		return ArrayUtils.clone(chunkDimensions);
	}

	/**
	 * @return the requested filters in pipeline (encode) order
	 */
	public List<RequestedFilter> getFilters() {
		return filters;
	}

	/**
	 * A filter and the settings it is applied with.
	 */
	public static final class RequestedFilter {
		private final Filter filter;
		private final int[] filterData;

		private RequestedFilter(Filter filter, int[] filterData) {
			this.filter = filter;
			this.filterData = filterData;
		}

		public Filter getFilter() {
			return filter;
		}

		public int[] getFilterData() {
			return ArrayUtils.clone(filterData);
		}
	}

	public static final class Builder {

		private int[] chunkDimensions;
		private final List<RequestedFilter> filters = new ArrayList<>();
		private boolean unsigned;

		private Builder() {
		}

		/**
		 * Forces the underlying fixed point (integer) data type to be written as unsigned. This only applies when
		 * the dataset data is a supported fixed point (integer) array/scalar (byte/short/int/long); requesting
		 * unsigned for any other data type will result in an exception.
		 *
		 * @return this builder
		 */
		public Builder unsigned() {
			this.unsigned = true;
			return this;
		}

		/**
		 * Sets the chunk dimensions to use, enabling chunked storage. Must have the same rank as the dataset, and
		 * every chunk dimension must be at least 1 and no larger than the corresponding dataset dimension.
		 *
		 * @param chunkDimensions the dimensions of each chunk
		 * @return this builder
		 */
		public Builder chunkDimensions(int... chunkDimensions) {
			Objects.requireNonNull(chunkDimensions, "chunkDimensions cannot be null");
			if (chunkDimensions.length == 0) {
				throw new IllegalArgumentException("chunkDimensions cannot be empty");
			}
			for (int chunkDimension : chunkDimensions) {
				if (chunkDimension < 1) {
					throw new IllegalArgumentException("All chunk dimensions must be >= 1. chunkDimensions="
						+ ArrayUtils.toString(chunkDimensions));
				}
			}
			this.chunkDimensions = ArrayUtils.clone(chunkDimensions);
			return this;
		}

		/**
		 * Adds the byte shuffle filter. Rearranges the bytes of the data which usually improves the following
		 * compression. Should be added before e.g. {@link #deflate(int)}.
		 *
		 * @return this builder
		 */
		public Builder shuffle() {
			// The element size filter data is filled in when the dataset is written
			filters.add(new RequestedFilter(new ByteShuffleFilter(), ArrayUtils.EMPTY_INT_ARRAY));
			return this;
		}

		/**
		 * Adds the deflate (gzip) compression filter with the default compression level (6).
		 *
		 * @return this builder
		 */
		public Builder deflate() {
			return deflate(6);
		}

		/**
		 * Adds the deflate (gzip) compression filter.
		 *
		 * @param level the compression level 0-9. Higher levels compress better but are slower
		 * @return this builder
		 */
		public Builder deflate(int level) {
			if (level < 0 || level > 9) {
				throw new IllegalArgumentException("Deflate level must be in the range 0-9. level=" + level);
			}
			filters.add(new RequestedFilter(new DeflatePipelineFilter(), new int[]{level}));
			return this;
		}

		/**
		 * Adds a filter to the pipeline. The filter must support encoding, see {@link Filter#encode(byte[], int[])}.
		 * To read the file back the filter must also be registered with
		 * {@link io.jhdf.filter.FilterManager#addFilter(Filter)} which happens automatically for built-in filters
		 * and filters loaded via {@link java.util.ServiceLoader}.
		 *
		 * @param filter the filter to apply
		 * @param filterData the settings the filter is applied with, stored in the file. e.g. compression level
		 * @return this builder
		 */
		public Builder filter(Filter filter, int... filterData) {
			Objects.requireNonNull(filter, "filter cannot be null");
			filters.add(new RequestedFilter(filter, ArrayUtils.clone(filterData)));
			return this;
		}

		public DatasetCreationOptions build() {
			return new DatasetCreationOptions(this);
		}
	}
}
