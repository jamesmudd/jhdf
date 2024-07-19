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
import io.jhdf.storage.HdfFileChannel;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Objects;

import static io.jhdf.Utils.stripLeadingIndex;

public class FloatingPoint extends DataType implements OrderedDataType {

	public static final int CLASS_ID = 1;
	private static final int ORDER_BIT = 0;
	private static final int LOW_PADDING_BIT = 1;
	private static final int HIGH_PADDING_BIT = 2;
	private static final int INTERNAL_PADDING_BIT = 3;

	private final ByteOrder order;
	private final boolean lowPadding;
	private final boolean highPadding;
	private final boolean internalPadding;
	private final int mantissaNormalization;
	private final int signLocation;

	// Properties
	private final short bitOffset;
	private final short bitPrecision;
	private final byte exponentLocation;
	private final byte exponentSize;
	private final byte mantissaLocation;
	private final byte mantissaSize;
	private final int exponentBias;

	public FloatingPoint(ByteBuffer bb) {
		super(bb);

		if (classBits.get(6)) {
			throw new UnsupportedHdfException("VAX endian is not supported");
		}
		if (classBits.get(ORDER_BIT)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = classBits.get(LOW_PADDING_BIT);
		highPadding = classBits.get(HIGH_PADDING_BIT);
		internalPadding = classBits.get(INTERNAL_PADDING_BIT);

		mantissaNormalization = Utils.bitsToInt(classBits, 4, 2);
		signLocation = Utils.bitsToInt(classBits, 8, 8);

		// Properties
		bitOffset = bb.getShort();
		bitPrecision = bb.getShort();
		exponentLocation = bb.get();
		exponentSize = bb.get();
		mantissaLocation = bb.get();
		mantissaSize = bb.get();
		exponentBias = bb.getInt();
	}

	private FloatingPoint(int size,
						  int mantissaNormalization,
						  int signLocation,
						  short bitOffset,
						  short bitPrecision,
						  byte exponentLocation,
						  byte exponentSize,
						  byte mantissaLocation,
						  byte mantissaSize,
						  int exponentBias) {
		super(CLASS_ID, size);
		this.order = ByteOrder.nativeOrder();
		this.lowPadding = false;
		this.highPadding = false;
		this.internalPadding = false;
		this.mantissaNormalization = mantissaNormalization;
		this.signLocation = signLocation;
		this.bitOffset = bitOffset;
		this.bitPrecision = bitPrecision;
		this.exponentLocation = exponentLocation;
		this.exponentSize = exponentSize;
		this.mantissaLocation = mantissaLocation;
		this.mantissaSize = mantissaSize;
		this.exponentBias = exponentBias;

		classBits.set(ORDER_BIT, order.equals(ByteOrder.BIG_ENDIAN));
		classBits.set(LOW_PADDING_BIT, false);
		classBits.set(HIGH_PADDING_BIT, false);
		classBits.set(INTERNAL_PADDING_BIT, false);
		Utils.writeIntToBits(mantissaNormalization, classBits, 4, 2);
		Utils.writeIntToBits(signLocation, classBits, 8, 8);
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

	public boolean isInternalPadding() {
		return internalPadding;
	}

	public int getMantissaNormalization() {
		return mantissaNormalization;
	}

	public int getSignLocation() {
		return signLocation;
	}

	public short getBitOffset() {
		return bitOffset;
	}

	public short getBitPrecision() {
		return bitPrecision;
	}

	public byte getExponentLocation() {
		return exponentLocation;
	}

	public byte getExponentSize() {
		return exponentSize;
	}

	public byte getMantissaLocation() {
		return mantissaLocation;
	}

	public byte getMantissaSize() {
		return mantissaSize;
	}

	public int getExponentBias() {
		return exponentBias;
	}

	@Override
	public Class<?> getJavaType() {
		switch (bitPrecision) {
			case 16:
			case 32:
				return float.class;
			case 64:
				return double.class;
			default:
				throw new HdfTypeException("Unsupported signed fixed point data type");
		}
	}

	@Override
	public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final Object data = Array.newInstance(getJavaType(), dimensions);
		final ByteOrder byteOrder = getByteOrder();
		switch (getSize()) {
			case 2:
				fillData(data, dimensions, buffer.order(byteOrder).asShortBuffer());
				break;
			case 4:
				fillData(data, dimensions, buffer.order(byteOrder).asFloatBuffer());
				break;
			case 8:
				fillData(data, dimensions, buffer.order(byteOrder).asDoubleBuffer());
				break;
			default:
				throw new HdfTypeException(
					"Unsupported floating point type size " + getSize() + " bytes");
		}
		return data;
	}

	private static void fillData(Object data, int[] dims, ShortBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			float[] floatData = (float[]) data;
			for (int i = 0; i < dims[0]; i++) {
				short element = buffer.get();
				floatData[i] = toFloat(element);
			}
		}
	}

	/**
	 * This method converts 2 bytes (represented as a short) into a half precision float (represented by the java full
	 * precision float).
	 * <p>
	 * The method used is described here https://stackoverflow.com/a/6162687/4653517
	 *
	 * @param element the 2 byte short to convert to a half precision float
	 * @return the converted float
	 */
	public static float toFloat(short element) {
		int mant = element & 0x03ff; // 10 bits mantissa
		int exp = element & 0x7c00; // 5 bits exponent

		if (exp == 0x7c00) // NaN/Inf
			exp = 0x3fc00; // -> NaN/Inf
		else if (exp != 0) { // normalized value
			exp += 0x1c000; // exp - 15 + 127
		} else if (mant != 0) { // && exp==0 -> subnormal
			exp = 0x1c400; // make it normal
			do {
				mant <<= 1; // mantissa * 2
				exp -= 0x400; // decrease exp by 1
			} while ((mant & 0x400) == 0); // while not normal
			mant &= 0x3ff; // discard subnormal bit
		} // else +/-0 -> +/-0

		return Float.intBitsToFloat( // combine all parts
			(element & 0x8000) << 16 // sign  << ( 31 - 15 )
				| (exp | mant) << 13); // value << ( 23 - 10 )
	}

	private static void fillData(Object data, int[] dims, FloatBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((float[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, DoubleBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((double[]) data);
		}
	}

	@Override
	public ByteBuffer toBuffer() {
		return  super.toBufferBuilder()
			.writeShort(bitOffset)
			.writeShort(bitPrecision)
			.writeByte(exponentLocation)
			.writeByte(exponentSize)
			.writeByte(mantissaLocation)
			.writeByte(mantissaSize)
			.writeInt(exponentBias)
			.build();
	}

	public static final FloatingPoint FLOAT = new FloatingPoint(
		4,
		2,
		31,
		(short) 0,
		(short) 32,
		(byte) 23,
		(byte) 8,
		(byte) 0,
		(byte) 23,
		127);

	public static final FloatingPoint DOUBLE = new FloatingPoint(
		8, // size
		2,      // mantissa normalisation
		63,     // Sign location
		(short) 0,   // bit offset
		(short) 64,   // bit precision
		(byte) 52,  // exponent location
		(byte) 11,    // exponent size
		(byte) 0,      // mantissa location
		(byte) 52,     // mantissa size
		1023);   // exponent bias


	@Override
	public ByteBuffer encodeData(Object data) {
		Objects.requireNonNull(data, "Cannot encode null");

		final Class<?> type = Utils.getType(data);
		if (data.getClass().isArray()) {
			final int[] dimensions = Utils.getDimensions(data);
			final int totalElements = Arrays.stream(dimensions).reduce(1, Math::multiplyExact);
			final ByteBuffer buffer = ByteBuffer.allocate(totalElements * getSize())
				.order(order);
			if(type == float.class) {
				encodeFloatData(data, dimensions, buffer.asFloatBuffer(), true);
			} else if (type == Float.class) {
				encodeFloatData(data, dimensions, buffer.asFloatBuffer(), false);
			}  else if (type == double.class) {
				encodeDoubleData(data, dimensions, buffer.asDoubleBuffer(), true);
			} else if (type == Double.class) {
				encodeDoubleData(data, dimensions, buffer.asDoubleBuffer(), false);
			} else {
				throw new UnsupportedHdfException("Cant write type: " + type);
			}

			return buffer;
		} else {
			// Scalar dataset
			final ByteBuffer buffer = ByteBuffer.allocate(getSize()).order(order);

			if (type == Float.class) {
				buffer.asFloatBuffer().put((Float) data);
			} else if (type == Double.class) {
				buffer.asDoubleBuffer().put((Double) data);
			} else {
				throw new UnsupportedHdfException("Cant write scalar type: " + type);
			}

			return buffer;
		}
	}

	@Override
	public void writeData(Object data, int[] dimensions, HdfFileChannel hdfFileChannel) {
		final int fastDimSize = dimensions[dimensions.length - 1];

		final Class<?> type = Utils.getType(data);
		if (data.getClass().isArray()) {
			// This buffer is reused
			final ByteBuffer buffer = ByteBuffer.allocate(fastDimSize * getSize())
				.order(order);

			if(type == float.class) {
				writeFloatData(data, dimensions, buffer, hdfFileChannel, true);
			} else if (type == Float.class) {
				writeFloatData(data, dimensions, buffer, hdfFileChannel, false);
			}  else if (type == double.class) {
				writeDoubleData(data, dimensions, buffer, hdfFileChannel, true);
			} else if (type == Double.class) {
				writeDoubleData(data, dimensions, buffer, hdfFileChannel, false);
			} else {
				throw new UnsupportedHdfException("Cant write type: " + type);
			}
		} else {
			// Scalar
			final ByteBuffer buffer = ByteBuffer.allocate(getSize()).order(order);
			if (type == Float.class) {
				buffer.asFloatBuffer().put((Float) data);
			} else if (type == Double.class) {
				buffer.asDoubleBuffer().put((Double) data);
			} else {
				throw new UnsupportedHdfException("Cant write scalar type: " + type);
			}
			hdfFileChannel.write(buffer);
		}

	}

	private static void writeDoubleData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel, boolean primitive) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeDoubleData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel, primitive);
			}
		} else {
			if(primitive) {
				buffer.asDoubleBuffer().put((double[]) data);
			} else {
				buffer.asDoubleBuffer().put(ArrayUtils.toPrimitive((Double[]) data));
			}
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}

	private static void writeFloatData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel, boolean primitive) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeFloatData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel, primitive);
			}
		} else {
			if(primitive) {
				buffer.asFloatBuffer().put((float[]) data);
			} else {
				buffer.asFloatBuffer().put(ArrayUtils.toPrimitive((Float[]) data));
			}
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}

	private static void encodeFloatData(Object data, int[] dims, FloatBuffer buffer, boolean primitive) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				encodeFloatData(newArray, stripLeadingIndex(dims), buffer, primitive);
			}
		} else {
			if(primitive) {
				buffer.put((float[]) data);
			} else {
				buffer.put(ArrayUtils.toPrimitive((Float[]) data));
			}
		}
	}

	private static void encodeDoubleData(Object data, int[] dims, DoubleBuffer buffer, boolean primitive) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				encodeDoubleData(newArray, stripLeadingIndex(dims), buffer, primitive);
			}
		} else {
			if(primitive) {
				buffer.put((double[]) data);
			} else {
				buffer.put(ArrayUtils.toPrimitive((Double[]) data));
			}
		}
	}


}
