package io.jhdf.links;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.AttributeMessage;

public class ExternalLink implements Link {

	private final String targetFile;
	private final String targetPath;
	private final String name;
	private final Group parent;

	private final LazyInitializer<Node> targetNode;

	public ExternalLink(String targetFile, String targetPath, String name, Group parent) {
		this.targetFile = targetFile;
		this.targetPath = targetPath;
		this.name = name;
		this.parent = parent;

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
	public Group getParent() {
		return parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return parent.getPath() + name;
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		return getTarget().getAttributes();
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
	public NodeType getType() {
		return getTarget().getType();
	}

	@Override
	public boolean isGroup() {
		return getTarget().isGroup();
	}

	@Override
	public File getFile() {
		return parent.getFile();
	}

	@Override
	public long getAddress() {
		return getTarget().getAddress();
	}

	@Override
	public HdfFile getHdfFile() {
		return parent.getHdfFile();
	}

	@Override
	public String getTargetPath() {
		return targetFile + ":" + targetPath;
	}

	@Override
	public boolean isLink() {
		return true;
	}

}
