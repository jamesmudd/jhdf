package io.jhdf.links;

import java.io.File;
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

public class SoftLink implements Link {

	private final String target;
	private final String name;
	private final Group parent;

	private final LazyInitializer<Node> targetNode;

	public SoftLink(String target, String name, Group parent) {
		this.target = target;
		this.name = name;
		this.parent = parent;

		targetNode = new LinkTargetLazyInitializer();
	}

	private class LinkTargetLazyInitializer extends LazyInitializer<Node> {
		@Override
		protected Node initialize() throws ConcurrentException {
			return parent.getHdfFile().getByPath(target);
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
		} catch (ConcurrentException e) {
			throw new HdfException("Could not resolve link target '" + target + "' from link '" + getPath() + "'");
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

}
