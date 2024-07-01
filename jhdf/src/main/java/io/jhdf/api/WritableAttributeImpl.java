/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api;

import io.jhdf.Utils;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataSpace;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public class WritableAttributeImpl implements Attribute {

	private final String name;
	private final Node node;
	private final Object data;
	private final DataType dataType;
	private final DataSpace dataSpace;

	public WritableAttributeImpl(String name, Node node, Object data) {
		this.name = name;
		this.node = node;
		this.data = data;
		this.dataType = DataType.fromObject(data);
		this.dataSpace = DataSpace.fromObject(data);
	}
	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getSize() {
		return dataSpace.getTotalLength();
	}

	@Override
	public long getSizeInBytes() {
		return getSize() * dataType.getSize();
	}

	@Override
	public int[] getDimensions() {
		return Utils.getDimensions(data);
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public Class<?> getJavaType() {
		final Class<?> type = dataType.getJavaType();
		// For scalar datasets the returned type will be the wrapper class because
		// getData returns Object
		if (isScalar() && type.isPrimitive()) {
			return primitiveToWrapper(type);
		}
		return type;
	}

	@Override
	public boolean isScalar() {
		return getDimensions().length == 0;
	}

	@Override
	public boolean isEmpty() {
		return data == null;
	}

	@Override
	public ByteBuffer getBuffer() {
		return dataType.encodeData(data);
	}

	@Override
	public DataSpace getDataSpace() {
		return dataSpace;
	}

	@Override
	public DataType getDataType() {
		return dataType;
	}
}
