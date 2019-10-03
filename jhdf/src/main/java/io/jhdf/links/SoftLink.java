/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.links;

import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.exceptions.HdfBrokenLinkException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

/**
 * Soft (symbolic) link to another {@link Node} in the HDF5 file.
 *
 * @author James Mudd
 */
public class SoftLink extends AbstractLink implements Link {

	private final String target;

	public SoftLink(String target, String name, Group parent) {
		super(name, parent);
		this.target = target;

		targetNode = new LinkTargetLazyInitializer();
	}

	private class LinkTargetLazyInitializer extends LazyInitializer<Node> {
		@Override
		protected Node initialize() {
			return parent.getHdfFile().getByPath(target);
		}
	}

	@Override
	public Node getTarget() {
		try {
			return targetNode.get();
		} catch (Exception e) {
			throw new HdfBrokenLinkException(
					"Could not resolve link target '" + target + "' from link '" + getPath() + "'", e);
		}
	}

	@Override
	public String getTargetPath() {
		return target;
	}

	@Override
	public String toString() {
		return "SoftLink [name=" + name + ", target=" + target + "]";
	}

}
