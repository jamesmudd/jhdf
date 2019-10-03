/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api;

import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataLayout;

/**
 * HDF5 dataset. Datasets contain the real data within a HDF5 file.
 *
 * @author James Mudd
 */
public interface Dataset extends Node {

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
	 * Checks if this dataset is scalar i.e is a single element with no dimensions.
	 *
	 * @return <code>true</code> if dataset if scalar <code>false</code> otherwise
	 */
	boolean isScalar();

	/**
	 * Checks if this dataset is empty i.e holds no data and no storage is
	 * allocated.
	 *
	 * @return <code>true</code> if dataset if empty <code>false</code> otherwise
	 */
	boolean isEmpty();


	/**
	 * Checks if this dataset is a compound dataset. i.e. it contains several named datasets.
	 *
	 * @return <code>true</code> if this is a compount dataset, <code>false</code> otherwise
	 */
	boolean isCompound();

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

	/**
	 * Gets the data from the HDF5 dataset and converts it to a Java object.
	 * <p>
	 * The returned type will be either:
	 * <ul>
	 * <li>A Java object of the type returned by {@link #getJavaType()} if the
	 * dataset is scalar ({@link #isScalar()}).</li>
	 * <li>A Java array of dimensions of the dataset as returned by
	 * {@link #getDimensions()}. The type of the array will be the return value of
	 * {@link #getJavaType()}.</li>
	 * <li>A Java {@link java.util.Map} if {@link #isCompound()} returns <code>true</code></li>
	 * <li><code>null</code> if the dataset is empty ({@link #isEmpty()}).</li>
	 * </ul>
	 *
	 * @return the data in the dataset as a Java object or <code>null</code> if the
	 *         dataset is empty.
	 */
	Object getData();

	/**
	 * Gets the Java type that will be used to represent this data.
	 *
	 * @return the Java type used to represent this dataset
	 */
	Class<?> getJavaType();

	/**
	 * Gets the {@link DataType} that is used to represent the data in the HDF5-file.
	 *
	 * @return the data type used to store the data in the HDF5-file
	 */
	DataType getDataType();

	/**
	 * Gets the fill value for this dataset or <code>null</code> if not defined. If
	 * it is defined the type will be that returned by {@link #getJavaType()}.
	 *
	 * @return the fill value of the dataset or <code>null</code> if not defined
	 */
	Object getFillValue();

}
