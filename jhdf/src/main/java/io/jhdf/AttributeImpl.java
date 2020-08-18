/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import io.jhdf.dataset.DatasetReader;
import io.jhdf.exceptions.HdfEmptyDatasetException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.AttributeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public class AttributeImpl implements Attribute {
	private static final Logger logger = LoggerFactory.getLogger(AttributeImpl.class);

	private final HdfFileChannel hdfFc;
	private final Node node;
	private final String name;
	private final AttributeMessage message;

	public AttributeImpl(HdfFileChannel hdfFc, Node node, AttributeMessage message) {
		this.hdfFc = hdfFc;
		this.node = node;
		this.name = message.getName();
		this.message = message;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public long getSize() {
		return message.getDataSpace().getTotalLength();
	}

	@Override
	public long getDiskSize() {
		return getSize() * message.getDataType().getSize();
	}

	@Override
	public int[] getDimensions() {
		return message.getDataSpace().getDimensions();
	}

	@Override
	public Object getData() {
		logger.debug("Getting data for attribute '{}' of '{}'...", name, node.getPath());
		if (isEmpty()) {
			throw new HdfEmptyDatasetException("Attribute [" + name + " on " + node.getPath() +"] is empty");
		}
		DataType type = message.getDataType();
		ByteBuffer bb = message.getDataBuffer();
		return DatasetReader.readDataset(type, bb, getDimensions(), hdfFc);
	}

	@Override
	public boolean isEmpty() {
		return message.getDataBuffer() == null;
	}

	@Override
	public boolean isScalar() {
		if (isEmpty()) {
			return false;
		}
		return getDimensions().length == 0;
	}

	@Override
	public Class<?> getJavaType() {
		final Class<?> type = message.getDataType().getJavaType();
		// For scalar datasets the returned type will be the wrapper class because
		// getData returns Object
		if (isScalar() && type.isPrimitive()) {
			return primitiveToWrapper(type);
		}
		return type;
	}

	@Override
	public ByteBuffer getBuffer() {
		return message.getDataBuffer();
	}
}
