/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see LICENSE file
 */
package io.jhdf.api;

import java.nio.ByteBuffer;

/**
 * Interface for HDF5 attributes. Attributes can be attached to {@link Group}s
 * or {@link Dataset}s. They contain metadata about that object.
 *
 * @author James Mudd
 */
public interface Attribute {

	/**
	 * Gets the {@link Node} which contains this {@link Attribute}
	 *
	 * @return the node this attribute is attached to
	 */
	Node getNode();

	/**
	 * Gets the name of this {@link Attribute}
	 *
	 * @return the name of this attribute
	 */
	String getName();

	/**
	 * Gets the total number of elements in this attributes dataset.
	 *
	 * @return the total number of elements in this attributes dataset
	 * @see Dataset#getSize()
	 */
	long getSize();

	/**
	 * Gets the size of this dataset. <blockquote>i.e. number of elements * size of each element</blockquote>
	 *
	 * @return the total number of bytes the dataset is using
	 * @see Dataset#getSizeInBytes()
	 */
	long getDiskSize();

	/**
	 * Gets the dimensions of this attributes dataset
	 *
	 * @return the dimensions of this attributes dataset
	 * @see Dataset#getDimensions()
	 */
	int[] getDimensions();

	/**
	 * Gets the data from the attributes dataset and converts it into a Java array
	 * of dimensions of the dataset as returned by {@link #getDimensions()}. The
	 * type of the array will be the return value of {@link #getJavaType()}.
	 *
	 * @return the data in the dataset as a Java array
	 * @see Dataset#getData()
	 */
	Object getData();

	/**
	 * Gets the Java type that will be used to represent this attributes data.
	 *
	 * @return the Java type used to represent this attributes dataset
	 * @see Dataset#getJavaType()
	 */
	Class<?> getJavaType();

	/**
	 * Checks if this dataset is scalar i.e is a single element with no dimensions.
	 *
	 * @return <code>true</code> if dataset if scalar <code>false</code> otherwise
	 * @see Dataset#isScalar()
	 */
	boolean isScalar();

	/**
	 * Checks if this dataset is empty i.e holds no data and no storage is
	 * allocated.
	 *
	 * @return <code>true</code> if dataset if empty <code>false</code> otherwise
	 * @see Dataset#isEmpty()
	 */
	boolean isEmpty();

	/**
	 * Gets the buffer for this attribute
	 *
	 * @return the buffer for this attribute
	 */
	ByteBuffer getBuffer();


}
