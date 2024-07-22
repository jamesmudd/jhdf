/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Fill Value (Old) Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#OldFillValueMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class FillValueOldMessage extends Message {

	public static final int MESSAGE_TYPE = 4;

	private final ByteBuffer fillValue;

	/* package */ FillValueOldMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		final int size = Utils.readBytesAsUnsignedInt(bb, 4);
		fillValue = Utils.createSubBuffer(bb, size);
	}

	/**
	 * The fill value. The bytes of the fill value are interpreted using the same
	 * datatype as for the dataset.
	 *
	 * @return a buffer containing the fill value
	 */
	public ByteBuffer getFillValue() {
		return fillValue.asReadOnlyBuffer();
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}


}
