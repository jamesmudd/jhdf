package io.jhdf.exceptions;

public class InvalidSliceHdfException extends HdfException {
	final long[] offset;
	final int[] sliceDimensions;
	final int[] datasetDimensions;

	public InvalidSliceHdfException(String message, long[] offset, int[] sliceDimensions, int[] datasetDimensions) {
		super(message);
		this.offset = offset;
		this.sliceDimensions = sliceDimensions;
		this.datasetDimensions = datasetDimensions;
	}
}
