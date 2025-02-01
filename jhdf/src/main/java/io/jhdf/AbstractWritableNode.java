/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.WritableAttributeImpl;
import io.jhdf.api.WritableNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractWritableNode implements WritableNode {
	private final Group parent;
	private final String name;

	private final Map<String, Attribute> attributes = new HashMap<>();

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
		return attributes;
	}

	@Override
	public Attribute getAttribute(String name) {
		return attributes.get(name);
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

	@Override
	public Attribute putAttribute(String name, Object data) {
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		Objects.requireNonNull(data, "Cannot write null attributes");
		WritableAttributeImpl attribute = new WritableAttributeImpl(name, this, data);
		return attributes.put(name, attribute);
	}

	@Override
	public Attribute removeAttribute(String name) {
		return attributes.remove(name);
	}
}
