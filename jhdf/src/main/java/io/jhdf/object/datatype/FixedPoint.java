/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static io.jhdf.Utils.flatten;
import static io.jhdf.Utils.stripLeadingIndex;

public class FixedPoint extends DataType implements OrderedDataType, WritableDataType {

	public static final int CLASS_ID = 0;
	private static final int ORDER_BIT = 0;
	private static final int LOW_PADDING_BIT = 1;
	private static final int HIGH_PADDING_BIT = 2;
	private static final int SIGNED_BIT = 3;
	private final ByteOrder order;
	private final boolean lowPadding;
	private final boolean highPadding;
	private final boolean signed;
	private final short bitOffset;
	private final short bitPrecision;

	public FixedPoint(ByteBuffer bb) {
		super(bb);

		if (classBits.get(ORDER_BIT)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = classBits.get(LOW_PADDING_BIT);
		highPadding = classBits.get(HIGH_PADDING_BIT);
		signed = classBits.get(SIGNED_BIT);

		bitOffset = bb.getShort();
		bitPrecision = bb.getShort();
	}

	public FixedPoint(int bytePrecision) {
		// TODO arg validation
		super(CLASS_ID, bytePrecision);
		this.order = ByteOrder.nativeOrder();
		this.bitPrecision = (short) (bytePrecision * 8);
		this.lowPadding = false;
		this.highPadding = false;
		this.signed = true;
		this.bitOffset = 0; // TODO ok?
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

	public boolean isSigned() {
		return signed;
	}

	public short getBitOffset() {
		return bitOffset;
	}

	public short getBitPrecision() {
		return bitPrecision;
	}

	@Override
	public Class<?> getJavaType() {
		if (signed) {
			switch (bitPrecision) {
				case 8:
					return byte.class;
				case 16:
					return short.class;
				case 32:
					return int.class;
				case 64:
					return long.class;
				default:
					throw new HdfTypeException("Unsupported signed fixed point data type");
			}
		} else { // Unsigned need promotion for Java
			switch (bitPrecision) {
				case 8: // Just go to int could go to short by java short support is poor
				case 16:
					return int.class;
				case 32:
					return long.class;
				case 64:
					return BigInteger.class;
				default:
					throw new HdfTypeException("Unsupported unsigned fixed point data type");
			}
		}
	}

	@Override
	public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final Object data = Array.newInstance(getJavaType(), dimensions);
		final ByteOrder byteOrder = getByteOrder();
		if (isSigned()) {
			switch (getSize()) {
				case 1:
					fillData(data, dimensions, buffer.order(byteOrder));
					break;
				case 2:
					fillData(data, dimensions, buffer.order(byteOrder).asShortBuffer());
					break;
				case 4:
					fillData(data, dimensions, buffer.order(byteOrder).asIntBuffer());
					break;
				case 8:
					fillData(data, dimensions, buffer.order(byteOrder).asLongBuffer());
					break;
				default:
					throw new HdfTypeException(
						"Unsupported signed integer type size " + getSize() + " bytes");
			}
		} else { // Unsigned
			switch (getSize()) {
				case 1:
					fillDataUnsigned(data, dimensions, buffer.order(byteOrder));
					break;
				case 2:
					fillDataUnsigned(data, dimensions, buffer.order(byteOrder).asShortBuffer());
					break;
				case 4:
					fillDataUnsigned(data, dimensions, buffer.order(byteOrder).asIntBuffer());
					break;
				case 8:
					fillDataUnsigned(data, dimensions, buffer.order(byteOrder).asLongBuffer());
					break;
				default:
					throw new HdfTypeException(
						"Unsupported unsigned integer type size " + getSize() + " bytes");
			}
		}
		return data;
	}

    @Override
    public ByteBuffer encodeData(Object data) {
		Class<?> type = Utils.getArrayType(data);
		// TODO multi dimensional and scalar and empty
		if(type == int.class) {
			Object[] flattened = flatten(data);
			ByteBuffer buffer = ByteBuffer.allocate(flattened.length * 4);
			buffer.asIntBuffer().put((int[]) data);
			return buffer;
		}
		throw new UnsupportedHdfException("Cant write type");
	}

    // Signed Fixed Point

	private static void fillData(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((byte[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, ShortBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((short[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, IntBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((int[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, LongBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((long[]) data);
		}
	}

	// Unsigned Fixed Point

	private static void fillDataUnsigned(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final byte[] tempBuffer = new byte[dims[0]];
			buffer.get(tempBuffer);
			// Convert to unsigned
			int[] intData = (int[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				intData[i] = Byte.toUnsignedInt(tempBuffer[i]);
			}
		}
	}

	private static void fillDataUnsigned(Object data, int[] dims, ShortBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final short[] tempBuffer = new short[dims[0]];
			buffer.get(tempBuffer);
			// Convert to unsigned
			int[] intData = (int[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				intData[i] = Short.toUnsignedInt(tempBuffer[i]);
			}
		}
	}

	private static void fillDataUnsigned(Object data, int[] dims, IntBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final int[] tempBuffer = new int[dims[0]];
			buffer.get(tempBuffer);
			// Convert to unsigned
			long[] longData = (long[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				longData[i] = Integer.toUnsignedLong(tempBuffer[i]);
			}
		}
	}

	private static void fillDataUnsigned(Object data, int[] dims, LongBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final long[] tempBuffer = new long[dims[0]];
			final ByteBuffer tempByteBuffer = ByteBuffer.allocate(8);
			buffer.get(tempBuffer);
			BigInteger[] bigIntData = (BigInteger[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				tempByteBuffer.putLong(0, tempBuffer[i]);
				bigIntData[i] = new BigInteger(1, tempByteBuffer.array());
			}
		}
	}

	@Override
	public ByteBuffer toBuffer() {
		classBits.set(ORDER_BIT, order.equals(ByteOrder.BIG_ENDIAN));
		classBits.set(LOW_PADDING_BIT, lowPadding);
		classBits.set(HIGH_PADDING_BIT, highPadding);
		classBits.set(SIGNED_BIT, signed);

		return  super.toBufferBuilder()
			.writeShort(bitOffset)
			.writeShort(bitPrecision)
			.build();
	}
}
