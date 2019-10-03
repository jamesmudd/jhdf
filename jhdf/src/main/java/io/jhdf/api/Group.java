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

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfInvalidPathException;

import java.util.Map;

/**
 * HDF5 group. Groups can be considered the directories inside the HDF5
 * hierarchy.
 *
 * @author James Mudd
 */
public interface Group extends Node, Iterable<Node> {

	/**
	 * Gets the children in this group, i.e other groups and datasets contained
	 * inside this group.
	 *
	 * @return map of name to {@link Node} of this groups children
	 */
	Map<String, Node> getChildren();

	/**
	 * Gets a child of this group by name. This is equivalent to calling
	 * {@link #getChildren()}.get(name).
	 *
	 * @param name the name of the requested child
	 * @return the child or <code>null</code> if there is no child
	 * @throws HdfException if getting the children fails
	 */
	Node getChild(String name);

	/**
	 * Gets a {@link Node} by path. The path given will be relative to this node.
	 *
	 * @param path the path to the requested node
	 * @return the node
	 * @throws HdfInvalidPathException if getting the path fails
	 */
	Node getByPath(String path);

	/**
	 * Gets a {@link Dataset} by path. The path given will be relative to this node.
	 *
	 * @param path the path to the requested dataset
	 * @return the dataset
	 * @throws HdfInvalidPathException if getting the path fails
	 */
	Dataset getDatasetByPath(String path);

	/**
	 * If <code>true</code> the insertion order of links into this {@link Group} is
	 * tracked (recorded) in the file.
	 *
	 * @return <code>true</code> if link creation order is tracked,
	 *         <code>false</code> otherwise
	 */
	boolean isLinkCreationOrderTracked();
}
