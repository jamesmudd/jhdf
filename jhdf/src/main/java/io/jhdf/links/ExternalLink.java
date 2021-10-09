/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.links;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.exceptions.HdfBrokenLinkException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Link to a {@link Node} in an external HDF5 file. The link is made of both a
 * target HDF5 file and a target path to a {@link Node} within the target file.
 *
 * @author James Mudd
 */
public class ExternalLink extends AbstractLink {

	private final String targetFile;
	private final String targetPath;

	public ExternalLink(String targetFile, String targetPath, String name, Group parent) {
		super(name, parent);
		this.targetFile = targetFile;
		this.targetPath = targetPath;

		targetNode = new ExternalLinkTargetLazyInitializer();
	}

	private class ExternalLinkTargetLazyInitializer extends LazyInitializer<Node> {
		@Override
		protected Node initialize() {
			// Open the external file
			final HdfFile externalFile = new HdfFile(getTargetFile());
			// Tell this file about it to keep track of open external files
			getHdfFile().addExternalFile(externalFile);
			return externalFile.getByPath(targetPath);
		}

		private Path getTargetFile() {
			// Check if the target file path is absolute
			if (targetFile.startsWith(File.separator)) {
				return Paths.get(targetFile);
			} else {
				// Need to resolve the full path
				Path thisFilesDirectory = parent.getFileAsPath().getParent();
				return thisFilesDirectory != null ? thisFilesDirectory.resolve(targetFile) : null;
			}
		}
	}

	@Override
	public Node getTarget() {
		try {
			return targetNode.get();
		} catch (Exception e) {
			throw new HdfBrokenLinkException(
				"Could not resolve link target '" + targetPath + "' in external file '" + targetFile
					+ "' from link '" + getPath() + "'",
				e);
		}
	}

	@Override
	public String getTargetPath() {
		return targetFile + ":" + targetPath;
	}

	@Override
	public String toString() {
		return "ExternalLink [name=" + name + ", targetFile=" + targetFile + ", targetPath=" + targetPath + "]";
	}

}
