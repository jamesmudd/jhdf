package io.jhdf.object.datatype;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

class FixedPoint extends DataType {
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

	public ByteOrder getOrder() {
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
}