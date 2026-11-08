/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.links;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.exceptions.HdfBrokenLinkException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

/**
 * Soft (symbolic) link to another {@link Node} in the HDF5 file.
 *
 * @author James Mudd
 */
public class SoftLink extends AbstractLink {

	private final String target;

	public SoftLink(String target, String name, Group parent) {
		super(name, parent);
		this.target = target;

		targetNode = new LinkTargetLazyInitializer();
	}

	private class LinkTargetLazyInitializer extends LazyInitializer<Node> {
		@Override
		protected Node initialize() {
			return LinkResolutionGuard.resolve(getResolutionKey(), () -> parent.getHdfFile().getByPath(target));
		}
	}

	private String getResolutionKey() {
		final HdfFile hdfFile = getHdfFile();
		final String fileKey = hdfFile.inMemory()
			? "memory:" + System.identityHashCode(hdfFile)
			: hdfFile.getFileAsPath().toAbsolutePath().normalize().toString();
		return "soft:" + fileKey + ":" + getPath();
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
