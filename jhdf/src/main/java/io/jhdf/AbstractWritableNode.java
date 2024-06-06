/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.WritableNode;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractWritableNode implements WritableNode {
	private final Group parent;
	private final String name;

	AbstractWritableNode(Group parent, String name) {
		this.parent = parent;
		this.name = name;
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
	public File getFile() {
		return parent.getFile();
	}

	@Override
	public Path getFileAsPath() {
		return parent.getFileAsPath();
	}

	@Override
	public HdfFile getHdfFile() {
		return parent.getHdfFile();
	}
}
