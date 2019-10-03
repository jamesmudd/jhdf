/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

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

	private static final int FILL_VALUE_DEFINED_BIT = 5;

	private final int spaceAllocationTime;
	private final int fillValueWriteTime;
	private final boolean fillValueDefined;
	private final ByteBuffer fillValue;

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
			BitSet flags = BitSet.valueOf(new byte[] { bb.get() });
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
