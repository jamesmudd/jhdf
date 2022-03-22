package io.jhdf.exceptions;

import java.util.Arrays;

public class InvalidSliceHdfException extends HdfException {
	final long[] sliceOffset;
	final int[] sliceDimensions;
	final int[] datasetDimensions;

	public InvalidSliceHdfException(String message, long[] sliceOffset, int[] sliceDimensions, int[] datasetDimensions) {
		super(message);
		this.sliceOffset = sliceOffset;
		this.sliceDimensions = sliceDimensions;
		this.datasetDimensions = datasetDimensions;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + "sliceOffset=" + Arrays.toString(sliceOffset) +
			", sliceDimensions=" + Arrays.toString(sliceDimensions) +
			", datasetDimensions=" + Arrays.toString(datasetDimensions) +
			'}' ;
	}
}
