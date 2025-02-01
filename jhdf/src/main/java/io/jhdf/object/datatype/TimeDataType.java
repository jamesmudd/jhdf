/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.storage.HdfBackingStorage;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

import static io.jhdf.Utils.stripLeadingIndex;

public class TimeDataType extends DataType implements OrderedDataType {
	private final ByteOrder order;
	private final short bitPrecision;

	public TimeDataType(ByteBuffer bb) {
		super(bb);

		if (classBits.get(0)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		bitPrecision = bb.getShort();
	}

	@Override
	public ByteOrder getByteOrder() {
		return order;
	}
	@Override
	public Class<?> getJavaType() {
		return byte[].class;
	}

	@Override
	public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final Object data = Array.newInstance(getJavaType(), dimensions);
		fillData(data, dimensions, buffer.order(getByteOrder()));
		return data;
	}

	private void fillData(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			for (int i = 0; i < Array.getLength(data); i++) {
				byte[] bytes = new byte[getSize()];
				buffer.get(bytes);
				Array.set(data, i, bytes);
			}
		}
	}

	public short getBitPrecision() {
		return bitPrecision;
	}
}
