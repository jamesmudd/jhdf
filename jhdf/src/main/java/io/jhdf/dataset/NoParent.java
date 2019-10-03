/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;

import java.io.File;
import java.util.Iterator;
import java.util.Map;


/**
 * A placeholder for a {@link Group} to use when the group of a {@link Dataset} is not known because it is specified by
 * a {@link io.jhdf.object.datatype.Reference}. Allows to do
 * {@code DatasetLoader.createDataset(hdfFc, linkHeader, "Unknown dataset", NoParent.INSTANCE)}.
 */
public enum NoParent implements Group {

	INSTANCE;

	private static final String UNKNOWN_GROUP = "Unknown group";
	private static final String UNKNOWN_NAME = "Unknown";

	@Override
	public Map<String, Node> getChildren() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public Node getChild(String name) {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public Node getByPath(String path) {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public Dataset getDatasetByPath(String path) {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public Group getParent() {
		return INSTANCE;
	}

	@Override
	public String getName() {
		return UNKNOWN_NAME;
	}

	@Override
	public String getPath() {
		return UNKNOWN_NAME + "/";
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public Attribute getAttribute(String name) {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public NodeType getType() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public HdfFile getHdfFile() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public long getAddress() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public boolean isLink() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}

	@Override
	public Iterator<Node> iterator() {
		throw new UnsupportedOperationException(UNKNOWN_GROUP);
	}
}
