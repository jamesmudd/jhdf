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

import io.jhdf.exceptions.HdfBrokenLinkException;

/**
 * HDF5 links interface. Used for soft (symbolic) links and external links
 *
 * @author James Mudd
 */
public interface Link extends Node {

	/**
	 * Resolves the link and returns the {@link Node} the link points to. If
	 * {@link #isBrokenLink()} returns <code>true</code> calling this method will
	 * throw.
	 *
	 * @return the {@link Node} this link points to
	 * @throws HdfBrokenLinkException if the link is broken
	 */
	Node getTarget();

	/**
	 * Gets the path this link points to, obtaining it will not require the link to
	 * be resolved.
	 *
	 * @return the path this link points to
	 */
	String getTargetPath();

	/**
	 * Checks if the link is valid i.e can be resolved.
	 *
	 * @return <code>true</code> if the link is broken (i.e. cannot be resolved)
	 */
	boolean isBrokenLink();

}
