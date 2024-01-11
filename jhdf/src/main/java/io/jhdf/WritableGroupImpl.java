package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WritableGroupImpl implements WritableGroup {

	private final Map<String, Node> children = new ConcurrentHashMap<>();

	private final Group parent;
	private final String name; // TODO Node superclass

	public WritableGroupImpl(Group parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	@Override
	public Map<String, Node> getChildren() {
		return Collections.unmodifiableMap(children);
	}

	@Override
	public Node getChild(String name) {
		return children.get(name);
	}

	@Override
	public Node getByPath(String path) {
		throw new UnsupportedHdfException("Not supported by writable groups");
	}

	@Override
	public Dataset getDatasetByPath(String path) {
		throw new UnsupportedHdfException("Not supported by writable groups");
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		return false;
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
		if (parent == null) {
			return "/" + getName();
		} else {
			return parent.getPath() + "/" + getName();
		}
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public Attribute getAttribute(String name) {
		return null;
	}

	@Override
	public NodeType getType() {
		return NodeType.GROUP;
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public Path getFileAsPath() {
		return null;
	}

	@Override
	public HdfFile getHdfFile() {
		return null;
	}

	@Override
	public long getAddress() {
		return 0;
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return false;
	}

	@Override
	public WritiableDataset putDataset(String name, Object data) {
		WritableDatasetImpl writableDataset = new WritableDatasetImpl(data, name, this);
		children.put(name, writableDataset);
		return writableDataset;
	}

	@Override
	public WritableGroup putGroup(String name) {
		WritableGroupImpl newGroup = new WritableGroupImpl(this, name);
		children.put(name, newGroup);
		return newGroup;
	}

	@Override
	public Iterator<Node> iterator() {
		return children.values().iterator();
	}
}
