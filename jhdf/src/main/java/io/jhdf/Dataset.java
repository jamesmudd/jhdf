package io.jhdf;

import java.util.Collections;
import java.util.Map;

import io.jhdf.object.message.AttributeMessage;

public class Dataset implements Node {

	private final String name;
	private final GroupImpl parent;
	private final Map<String, AttributeMessage> attributes;

	public Dataset(String name, GroupImpl parent) {
		this.name = name;
		this.parent = parent;
		attributes = Collections.emptyMap();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isGroup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Node> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return parent.getPath() + "/" + name;
	}

	public Map<String, AttributeMessage> getAttributes() {
		return attributes;
	}

}
