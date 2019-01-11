package io.jhdf.object.datatype;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

public class FloatingPoint extends DataType implements OrderedDataType {

	private final ByteOrder order;
	private final boolean lowPadding;
	private final boolean highPadding;
	private final boolean internalPadding;
	private final int mantissaNormalization;
	private final byte signLocation;

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

		BitSet bits = BitSet.valueOf(classBytes);
		if (bits.get(6)) {
			throw new UnsupportedHdfException("VAX endian is not supported");
		}
		if (bits.get(0)) {
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}

		lowPadding = bits.get(1);
		highPadding = bits.get(2);
		internalPadding = bits.get(3);

		// Mask the 4+5 bits and shift to the end
		mantissaNormalization = Utils.bitsToInt(bits, 4, 2);

		signLocation = classBytes[1];

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

	public byte getSignLocation() {
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

}
