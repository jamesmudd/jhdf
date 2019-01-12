package io.jhdf.links;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.concurrent.LazyInitializer;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.object.message.AttributeMessage;

public abstract class AbstractLink implements Link {

	protected final String name;
	protected final Group parent;
	protected LazyInitializer<Node> targetNode;

	public AbstractLink(String name, Group parent) {
		this.name = name;
		this.parent = parent;
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
	public HdfFile getHdfFile() {
		return parent.getHdfFile();
	}

	@Override
	public boolean isLink() {
		return true;
	}

	@Override
	public long getAddress() {
		return getTarget().getAddress();
	}

}