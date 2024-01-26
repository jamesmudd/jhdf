/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.BufferBuilder;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Fill Value Message. Used to specify a fill value for datasets.
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#FillValueMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class FillValueMessage extends Message {

	public static final int MESSAGE_TYPE = 5;

	private static final int FILL_VALUE_DEFINED_BIT = 5;

	private final int spaceAllocationTime;
	private final int fillValueWriteTime;
	private final boolean fillValueDefined;
	private final ByteBuffer fillValue;

	public static final FillValueMessage NO_FILL =
		new FillValueMessage(1, 0, false, null);

	/* package */ FillValueMessage(ByteBuffer bb, BitSet messageFlags) {
		super(messageFlags);

		final byte version = bb.get();
		if (version == 1 || version == 2) {
			spaceAllocationTime = bb.get();
			fillValueWriteTime = bb.get();
			boolean fillValueMaybeDefined = bb.get() == 1;

			if (version == 2 && fillValueMaybeDefined) {
				int size = Utils.readBytesAsUnsignedInt(bb, 4);
				if (size > 0) {
					fillValue = Utils.createSubBuffer(bb, size);
					fillValueDefined = true;
				} else {
					fillValue = null;
					fillValueDefined = false;
				}
			} else {
				fillValue = null; // No fill value defined
				fillValueDefined = false;
			}
		} else if (version == 3) {
			BitSet flags = BitSet.valueOf(new byte[]{bb.get()});
			spaceAllocationTime = Utils.bitsToInt(flags, 0, 2); // 0-1
			fillValueWriteTime = Utils.bitsToInt(flags, 2, 2); // 2-3
			fillValueDefined = flags.get(FILL_VALUE_DEFINED_BIT);

			if (fillValueDefined) {
				int size = Utils.readBytesAsUnsignedInt(bb, 4);
				fillValue = Utils.createSubBuffer(bb, size);
			} else {
				fillValue = null; // No fill value defined
			}
		} else {
			throw new HdfException("Unrecognized version = " + version);
		}
	}

	private FillValueMessage(int spaceAllocationTime, int fillValueWriteTime, boolean fillValueDefined, ByteBuffer fillValue) {
		this.spaceAllocationTime = spaceAllocationTime;
		this.fillValueWriteTime = fillValueWriteTime;
		this.fillValueDefined = fillValueDefined;
		this.fillValue = fillValue;
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

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public ByteBuffer toBuffer() {
		BitSet flags = new BitSet(8);
		Utils.writeIntToBits(spaceAllocationTime, flags, 0, 2);
		Utils.writeIntToBits(fillValueWriteTime, flags, 2, 2);
		Utils.writeIntToBits(fillValueDefined ? 0 : 1, flags, 4, 1);
		Utils.writeIntToBits(fillValueDefined ? 1 : 0, flags, 5, 1);
		return new BufferBuilder()
			.writeByte(3) // version
			.writeBitSet(flags, 1)
			// TODO Size + fill vale here
			.build();
	}

}
