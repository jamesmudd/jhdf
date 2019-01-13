package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class FillValueMessage extends Message {

	private static final int SPACE_ALLOCATION_TIME_MASK = 0b1100_0000;
	private static final int FILL_VALUE_TIME_MASK = 0b0011_0000;
	private static final int FILL_VALUE_UNDEFINED_MASK = 0b0000_1000;

	private final byte version;
	private final int spaceAllocationTime;
	private final int fillValueWriteTime;
	private final boolean fillValueDefined;
	private final ByteBuffer fillValue;

	/* package */ FillValueMessage(ByteBuffer bb, Superblock sb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();
		if (version == 1 || version == 2) {
			spaceAllocationTime = bb.get();
			fillValueWriteTime = bb.get();
			fillValueDefined = bb.get() == 1;

			if (version == 2 && fillValueDefined) {
				int size = Utils.readBytesAsUnsignedInt(bb, 4);
				fillValue = Utils.createSubBuffer(bb, size);
			} else {
				fillValue = null; // No fill value defined
			}
		} else if (version == 3) {
			byte flags = bb.get();
			spaceAllocationTime = flags & 0xff & SPACE_ALLOCATION_TIME_MASK >>> 6;
			fillValueWriteTime = flags & 0xff & FILL_VALUE_TIME_MASK >>> 4;
			fillValueDefined = (flags & 0xff & FILL_VALUE_UNDEFINED_MASK >>> 5) == 1;

			if (fillValueDefined) {
				int size = Utils.readBytesAsUnsignedInt(bb, 4);
				fillValue = Utils.createSubBuffer(bb, size);
			} else {
				fillValue = null; // No fill value defined
			}
		} else {
			throw new HdfException("Unreconized version = " + version);
		}
	}

	public boolean isFillValueDefined() {
		return fillValueDefined;
	}

	public int getSpaceAllocationTime() {
		return spaceAllocationTime;
	}

	public int getFillValueWriteTime() {
		return fillValueWriteTime;
	}

	public ByteBuffer getFillValue() {
		return fillValue.asReadOnlyBuffer();
	}
}
