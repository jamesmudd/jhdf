package io.jhdf.api;

import java.util.Map;

import io.jhdf.exceptions.HdfException;

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

}
