package io.jhdf.object.datatype;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

import io.jhdf.exceptions.HdfTypeException;

class FixedPoint extends DataType implements OrderedDataType {
	private final ByteOrder order;
	private final boolean lowPadding;
	private final boolean highPadding;
	private final boolean signed;
	private final short bitOffset;
	private final short bitPrecision;

	public FixedPoint(ByteBuffer bb) {
		super(bb);

		BitSet bits = BitSet.valueOf(classBytes);
		if (bits.get(0)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = bits.get(1);
		highPadding = bits.get(2);
		signed = bits.get(3);

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
			case 8:
				return short.class;
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