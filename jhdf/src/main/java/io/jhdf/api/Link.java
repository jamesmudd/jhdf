/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 * 
 * http://jhdf.io
 * 
 * Copyright 2019 James Mudd
 * 
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.api;

public interface Link extends Node {

	/**
	 * Resolves the link and returns the {@link Node} the link points to.
	 * 
	 * @return the {@link Node} this link points to
	 */
	Node getTarget();

	/**
	 * Gets the path this link points to, obtaining it will not require the link to
	 * be resolved.
	 * 
	 * @return the path this link points to
	 */
	String getTargetPath();

}
