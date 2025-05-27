/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see LICENSE file
 */
package io.jhdf.api;

import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataSpace;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Interface for HDF5 attributes. Attributes can be attached to {@link Group}s
 * or {@link Dataset}s. They contain metadata about that object.
 *
 * @author James Mudd
 */
@NullMarked
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
	 * Gets the size of this dataset. i.e. <blockquote>number of elements * size of each element</blockquote>
	 *
	 * @return the total number of bytes this attributes dataset is using
	 * @see Dataset#getSizeInBytes()
	 */
	long getSizeInBytes();

	/**
	 * Gets the dimensions of this attribute
	 *
	 * @return the dimensions of this attribute
	 * @see Dataset#getDimensions()
	 */
	int[] getDimensions();

	/**
	 * Gets the data from the attributes dataset and converts it into a Java array
	 * of dimensions of the dataset as returned by {@link #getDimensions()}. The
	 * type of the array will be the return value of {@link #getJavaType()}.
	 *
	 * @return the data in the attribute as a Java array or <code>null</code> if the attribute is empty
	 * @see Dataset#getData()
	 */
	@Nullable Object getData();

	/**
	 * Gets the Java type that will be used to represent this attributes data.
	 *
	 * @return the Java type used to represent this attributes dataset
	 * @see Dataset#getJavaType()
	 */
	Class<?> getJavaType();

	/**
	 * Checks if this attribute is scalar i.e is a single element with no dimensions.
	 *
	 * @return <code>true</code> if dataset if scalar <code>false</code> otherwise
	 * @see Dataset#isScalar()
	 */
	boolean isScalar();

	/**
	 * Checks if this attribute is empty i.e holds no data and no storage is
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


	DataSpace getDataSpace();

	DataType getDataType();
}
