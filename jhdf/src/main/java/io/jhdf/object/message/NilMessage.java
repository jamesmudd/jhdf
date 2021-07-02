/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Nil (Null) Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#NILMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class NilMessage extends Message {

	public static final int MESSAGE_TYPE = 0;

	private final int size;

	/* package */ NilMessage(ByteBuffer bb, BitSet flags) {
		super(flags);
		this.size = bb.capacity();
		// Move buffer to the end
		bb.position(bb.limit());
	}

	public static NilMessage create() {
		return new NilMessage(16);
	}

	private NilMessage(int size) {
		super(new BitSet(1));
		this.size = size;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public ByteBuffer toBuffer() {
		return ByteBuffer.allocate(size);
	}
}
