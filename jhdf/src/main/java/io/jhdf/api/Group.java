package io.jhdf.api;

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

}
