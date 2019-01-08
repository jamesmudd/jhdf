package io.jhdf;

import java.util.Map;

import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.object.message.AttributeMessage;

public abstract class AbstractNode implements Node {

	protected final long address;
	protected final String name;
	protected final Group parent;
	protected Map<String, AttributeMessage> attributes;

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
	public Map<String, AttributeMessage> getAttributes() {
		return attributes;
	}

	@Override
	public Group getParent() {
		return parent;
	}

	@Override
	public long getAddress() {
		return address;
	}

}