/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.exceptions.HdfTypeException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FixedPoint extends DataType implements OrderedDataType {
	private final ByteOrder order;
	private final boolean lowPadding;
	private final boolean highPadding;
	private final boolean signed;
	private final short bitOffset;
	private final short bitPrecision;

	public FixedPoint(ByteBuffer bb) {
		super(bb);

		if (classBits.get(0)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = classBits.get(1);
		highPadding = classBits.get(2);
		signed = classBits.get(3);

		bitOffset = bb.getShort();
		bitPrecision = bb.getShort();
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
				throw new HdfTypeException("Unsupported signed fixed point data type");
			}
		}
	}

}
