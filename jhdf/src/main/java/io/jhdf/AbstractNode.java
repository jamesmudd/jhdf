package io.jhdf;

import java.io.File;

import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;

public abstract class AbstractNode implements Node {

	protected final long address;
	protected final String name;
	protected final Group parent;

	public AbstractNode(long address, String name, Group parent) {
		this.address = address;
		this.name = name;
		this.parent = parent;
	}

	@Override
	public boolean isGroup() {
		return getType() == NodeType.GROUP;
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
	public Group getParent() {
		return parent;
	}

	@Override
	public long getAddress() {
		return address;
	}

	@Override
	public File getFile() {
		// Recurse back up to the file
		return getParent().getFile();

	}

}