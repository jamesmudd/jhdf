/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;
import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Object Header Continuation Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#ContinuationMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class ObjectHeaderContinuationMessage extends Message {

	public static final int MESSAGE_TYPE = 16;

	private final long offset;
	private final int length;

	/* package */ ObjectHeaderContinuationMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		super(flags);

		offset = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		length = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfOffsets());
	}

	public long getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}


}
