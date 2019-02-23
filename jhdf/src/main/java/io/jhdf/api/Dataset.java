package io.jhdf.api;

import java.nio.ByteBuffer;

import io.jhdf.object.message.DataLayout;

/**
 * HDF5 dataset. Datasets contain the real data within a HDF5 file.
 * 
 * @author James Mudd
 */
public interface Dataset extends Node {

	/**
	 * Gets the buffer that holds this datasets data. The returned buffer will be of
	 * the correct order (endiness).
	 * 
	 * @return the data buffer that holds this dataset
	 */
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

	/**
	 * Gets the dimensions of this dataset
	 * 
	 * @return the dimensions of this dataset
	 */
	int[] getDimensions();

	/**
	 * Gets the max size of this dataset. If not specified this will be equal to
	 * {@link #getDimensions()}
	 * 
	 * @return the max size of this dataset
	 */
	int[] getMaxSize();

	/**
	 * Gets the data layout of this dataset.
	 * 
	 * @return the data layout of this dataset
	 */
	DataLayout getDataLayout();

	Object getData();

	public Class<?> getJavaType();

}