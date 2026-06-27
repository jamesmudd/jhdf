/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

import java.util.Arrays;

public class InvalidSliceHdfException extends HdfException {
	final long[] sliceOffset;
	final int[] sliceDimensions;
	final long[] datasetDimensions;

	public InvalidSliceHdfException(String message, long[] sliceOffset, int[] sliceDimensions, long[] datasetDimensions) {
		super(message);
		this.sliceOffset = sliceOffset;
		this.sliceDimensions = sliceDimensions;
		this.datasetDimensions = datasetDimensions;
	}

	public InvalidSliceHdfException(String message, long[] sliceOffset, int[] sliceDimensions, int[] datasetDimensions) {
		this(message, sliceOffset, sliceDimensions, Arrays.stream(datasetDimensions).asLongStream().toArray());
	}

	@Override
	public String getMessage() {
		return super.getMessage() + "sliceOffset=" + Arrays.toString(sliceOffset) +
			", sliceDimensions=" + Arrays.toString(sliceDimensions) +
			", datasetDimensions=" + Arrays.toString(datasetDimensions) +
			'}' ;
	}
}
