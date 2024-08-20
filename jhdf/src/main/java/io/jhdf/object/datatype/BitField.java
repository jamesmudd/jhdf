/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.storage.HdfBackingStorage;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import static io.jhdf.Utils.stripLeadingIndex;

public class BitField extends DataType implements OrderedDataType {
	public static final int CLASS_ID = 4;
	private static final int ORDER_BIT = 0;
	private static final int LOW_PADDING_BIT = 1;
	private static final int HIGH_PADDING_BIT = 2;

	public static final BitField INSTANCE = new BitField();

	private final ByteOrder order;
	private final boolean lowPadding;
	private final boolean highPadding;
	private final short bitOffset;
	private final short bitPrecision;

	public BitField(ByteBuffer bb) {
		super(bb);

		if (classBits.get(ORDER_BIT)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = classBits.get(LOW_PADDING_BIT);
		highPadding = classBits.get(HIGH_PADDING_BIT);

		bitOffset = bb.getShort();
		bitPrecision = bb.getShort();
	}

	private BitField() {
		super(CLASS_ID, 1);

		this.order = ByteOrder.nativeOrder();
		this.bitPrecision = 8;
		this.bitOffset = 0;
		this.lowPadding = false;
		this.highPadding = false;
	}

	@Override
	public ByteOrder getByteOrder() {
		return order;
	}

	public boolean isLowPadding() {
		return lowPadding;
	}

	public boolean isHighPadding() {
		return highPadding;
	}

	public short getBitOffset() {
		return bitOffset;
	}

	public short getBitPrecision() {
		return bitPrecision;
	}

	@Override
	public Class<?> getJavaType() {
		return boolean.class;
	}

	@Override
	public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final Object data = Array.newInstance(getJavaType(), dimensions);
		fillBitfieldData(data, dimensions, BitSet.valueOf(buffer.order(getByteOrder())), new AtomicInteger());
		return data;
	}

	private void fillBitfieldData(Object data, int[] dims, BitSet bitset, AtomicInteger position) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillBitfieldData(newArray, stripLeadingIndex(dims), bitset, position);
			}
		} else {
			for (int i = 0; i < dims[0]; i++) {
				Array.set(data, i, bitset.get(position.getAndAdd(getBitPrecision())));
			}
		}
	}

	@Override
	public ByteBuffer toBuffer() {
		classBits.set(ORDER_BIT, order.equals(ByteOrder.BIG_ENDIAN));
		classBits.set(LOW_PADDING_BIT, lowPadding);
		classBits.set(HIGH_PADDING_BIT, highPadding);

		return  super.toBufferBuilder()
			.writeShort(bitOffset)
			.writeShort(bitPrecision)
			.build();
	}

}
