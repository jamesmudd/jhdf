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

import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
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

	@Override
	public ByteBuffer encodeData(Object data) {
		Objects.requireNonNull(data, "Cannot encode null");


		if(data.getClass().isArray()) {
			return encodeArrayData(data);
		} else {
			return encodeScalarData(data);
		}
	}


	private ByteBuffer encodeScalarData(Object data) {
		final ByteBuffer buffer = ByteBuffer.allocate(getSize()).order(order);
		buffer.put(booleanToByte((Boolean) data));
		return buffer;
	}

	private ByteBuffer encodeArrayData(Object data) {
		final Class<?> type = Utils.getType(data);
		final int[] dimensions = Utils.getDimensions(data);
		final int totalElements = Arrays.stream(dimensions).reduce(1, Math::multiplyExact);
		final ByteBuffer buffer = ByteBuffer.allocate(totalElements * getSize())
			.order(order);
		if(type == boolean.class) {
			encodeBooleanData(data, dimensions, buffer, true);
		} else if (type == Boolean.class) {
			encodeBooleanData(data, dimensions, buffer, false);
		} else {
			throw new UnsupportedHdfException("Cant write type: " + type);
		}
		return buffer;
	}

	private static void encodeBooleanData(Object data, int[] dims, ByteBuffer buffer, boolean primitive) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				encodeBooleanData(newArray, stripLeadingIndex(dims), buffer, primitive);
			}
		} else {
			if(primitive) {
				buffer.put(asByteArray((boolean[]) data));
			} else {
				buffer.put(asByteArray(ArrayUtils.toPrimitive((Boolean[]) data)));
			}
		}
	}

	private static byte[] asByteArray(boolean[] data) {
		byte[] bytes = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
            bytes[i] = booleanToByte(data[i]);
        }
		return bytes;
	}

	private static byte booleanToByte(boolean b) {
		return b ? (byte) 1 : 0;
	}

	@Override
	public void writeData(Object data, int[] dimensions, HdfFileChannel hdfFileChannel) {
		if (data.getClass().isArray()) {
			writeArrayData(data, dimensions, hdfFileChannel); // TODO
		} else {
			writeScalarData(data, hdfFileChannel);
		}

	}

	private void writeScalarData(Object data, HdfFileChannel hdfFileChannel) {
		ByteBuffer buffer = encodeScalarData(data);
		buffer.rewind();
		hdfFileChannel.write(buffer);
	}

	private void writeArrayData(Object data, int[] dimensions, HdfFileChannel hdfFileChannel) {
		final Class<?> type = Utils.getType(data);
		final int fastDimSize = dimensions[dimensions.length - 1];
		// This buffer is reused
		final ByteBuffer buffer = ByteBuffer.allocate(fastDimSize * getSize())
			.order(order);
		if (type == boolean.class) {
			writeBooleanData(data, dimensions, buffer, hdfFileChannel, true);
		} else if (type == Boolean.class) {
			writeBooleanData(data, dimensions, buffer, hdfFileChannel, false);
		} else {
			throw new UnsupportedHdfException("Cant write type: " + type);
		}
	}


	private static void writeBooleanData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel, boolean primitive) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeBooleanData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel, primitive);
			}
		} else {
			if(primitive) {
				buffer.put(asByteArray((boolean[]) data));
			} else {
				buffer.put(asByteArray(ArrayUtils.toPrimitive((Boolean[]) data)));
			}
			buffer.rewind(); // Need to rewind as there is not a view
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}

}
