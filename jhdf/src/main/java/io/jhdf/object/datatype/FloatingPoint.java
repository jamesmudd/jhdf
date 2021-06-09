/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.storage.HdfBackingStorage;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static io.jhdf.Utils.stripLeadingIndex;

public class FloatingPoint extends DataType implements OrderedDataType {

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
		if (classBits.get(0)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = classBits.get(1);
		highPadding = classBits.get(2);
		internalPadding = classBits.get(3);

		// Mask the 4+5 bits and shift to the end
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

}
