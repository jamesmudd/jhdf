/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import io.jhdf.dataset.DatasetReader;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.storage.HdfBackingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public class AttributeImpl implements Attribute {
	private static final Logger logger = LoggerFactory.getLogger(AttributeImpl.class);

	private final HdfBackingStorage hdfBackingStorage;
	private final Node node;
	private final String name;
	private final AttributeMessage message;

	public AttributeImpl(HdfBackingStorage hdfBackingStorage, Node node, AttributeMessage message) {
		this.hdfBackingStorage = hdfBackingStorage;
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
	public long getSizeInBytes() {
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
			return null;
		}
		DataType type = message.getDataType();
		ByteBuffer bb = message.getDataBuffer();
		return DatasetReader.readDataset(type, bb, getDimensions(), hdfBackingStorage);
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

	@Override
	public DataSpace getDataSpace() {
		return message.getDataSpace();
	}

	@Override
    public DataType getDataType() {
        return message.getDataType();
    }
}
