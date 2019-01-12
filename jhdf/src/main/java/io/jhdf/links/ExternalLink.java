package io.jhdf.links;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.exceptions.HdfException;

public class ExternalLink extends AbstractLink implements Link {

	final String targetFile;
	final String targetPath;

	public ExternalLink(String targetFile, String targetPath, String name, Group parent) {
		super(name, parent);
		this.targetFile = targetFile;
		this.targetPath = targetPath;

		targetNode = new ExternalLinkTargetLazyInitializer();
	}

	private class ExternalLinkTargetLazyInitializer extends LazyInitializer<Node> {
		@Override
		protected Node initialize() throws ConcurrentException {
			// Open the external file
			final HdfFile externalFile = new HdfFile(getTargetFile());
			// Tell this file about it to keep track of open external files
			getHdfFile().addExternalFile(externalFile);
			return externalFile.getByPath(targetPath);
		}

		private File getTargetFile() {
			// Check if the target file path is absolute
			if (targetFile.startsWith(File.separator)) {
				return Paths.get(targetFile).toFile();
			} else {
				// Need to resolve the full path
				String absolutePathOfThisFilesDirectory = parent.getFile().getParent();
				return Paths.get(absolutePathOfThisFilesDirectory, targetFile).toFile();
			}
		}
	}

	@Override
	public Node getTarget() {
		try {
			return targetNode.get();
		} catch (ConcurrentException | HdfException e) {
			throw new HdfException("Could not resolve link target '" + targetPath + "' in external file '" + targetFile
					+ "' from link '" + getPath() + "'", e);
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
