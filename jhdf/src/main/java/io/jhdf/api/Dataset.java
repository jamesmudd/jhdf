package io.jhdf.api;

import java.nio.ByteBuffer;

/**
 * HDF5 dataset. Datasets contain the real data within a HDF5 file.
 * 
 * @author James Mudd
 */
public interface Dataset extends Node {

	ByteBuffer getDataBuffer();

	/**
	 * Gets the total number of elements in this dataset.
	 * 
	 * @return the total number of elements in this dataset
	 */
	long getSize();

	/**
	 * Gets the disk size used by this dataset. <blockquote>i.e. number of elements
	 * * size of each element</blockquote>
	 * 
	 * @return the total number of bytes the dataset is using
	 */
	long getDiskSize();

}