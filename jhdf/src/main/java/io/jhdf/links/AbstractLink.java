/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 * 
 * http://jhdf.io
 * 
 * Copyright 2019 James Mudd
 * 
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.links;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.concurrent.LazyInitializer;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;

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
	public Map<String, Attribute> getAttributes() {
		return getTarget().getAttributes();
	}

	@Override
	public Attribute getAttribute(String name) {
		return getTarget().getAttribute(name);
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
