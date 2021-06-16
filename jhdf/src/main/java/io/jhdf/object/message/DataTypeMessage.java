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

import io.jhdf.object.datatype.DataType;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class DataTypeMessage extends Message {

	public static final int MESSAGE_TYPE = 3;

	private final DataType dataType;

	/* package */ DataTypeMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		dataType = DataType.readDataType(bb);
	}

	public DataType getDataType() {
		return dataType;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public ByteBuffer toBuffer() {
		return null;
	}

}
