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

import io.jhdf.HdfFile;

import java.io.File;
import java.util.Map;

/**
 * Represents all objects contained in a HDF5 file
 *
 * @author James Mudd
 */
public interface Node {

	/**
	 * Gets the {@link Group} which contains this {@link Node} i.e. this nodes
	 * parent.
	 *
	 * @return the parent of this node.
	 */
	Group getParent();

	/**
	 * Gets the name of this {@link Node}
	 *
	 * @return the name of this node
	 */
	String getName();

	/**
	 * Gets the absolute path within the file of this node including the nodes name.
	 * Separated by '/'
	 *
	 * @return the path to this node
	 */
	String getPath();

	/**
	 * Gets the attributes associated with this {@link Node}.
	 *
	 * @return map of attribute name to attribute
	 */
	Map<String, Attribute> getAttributes();

	/**
	 * Gets the attribute with the given name. This is equivalent to calling
	 * {@link #getAttributes()}.get(name).
	 *
	 * @param name of the requested attribute
	 * @return the attribute of the given name or <code>null</code> if not available
	 */
	Attribute getAttribute(String name);

	/**
	 * Gets the type of this {@link Node}.
	 *
	 * @return the type of this node
	 */
	NodeType getType();

	/**
	 * Checks if this {@link Node} is a {@link Group}.
	 *
	 * @return <code>true</code> if this node is a group <code>false</code>
	 *         otherwise
	 */
	boolean isGroup();

	/**
	 * Gets the {@link File} object containing this {@link Node}.
	 *
	 * @return the file containing this node
	 */
	File getFile();

	/**
	 * Gets the parent {@link HdfFile} of this node can be useful if you want to
	 * resolve an absolute path in this file.
	 *
	 * @return The HDF file containing this {@link Node}
	 */
	HdfFile getHdfFile();

	/**
	 * This returns the byte offset into the file of the object header for this
	 * node.
	 *
	 * @return The address of the object header for this node
	 */
	long getAddress();

	/**
	 * Checks if this {@link Node} is a {@link Link}.
	 *
	 * @return <code>true</code> if this {@link Node} is a link <code>false</code>
	 *         otherwise
	 */
	boolean isLink();

	/**
	 * If <code>true</code> the insertion order of attributes is tracked (recorded)
	 * in the file.
	 *
	 * @return <code>true</code> if attribute creation order is tracked.
	 */
	boolean isAttributeCreationOrderTracked();

}
