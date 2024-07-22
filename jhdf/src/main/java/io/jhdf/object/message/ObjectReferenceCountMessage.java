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

import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class ObjectReferenceCountMessage extends Message {

	public static final int MESSAGE_TYPE = 22;
	private final int referenceCount;

	public ObjectReferenceCountMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unrecognized version = " + version);
		}

		referenceCount = bb.getInt();
	}

	public int getReferenceCount() {
		return referenceCount;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}


}
