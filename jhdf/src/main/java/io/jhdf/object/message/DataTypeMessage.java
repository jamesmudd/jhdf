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

import io.jhdf.object.datatype.DataType;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class DataTypeMessage extends Message {

	private final DataType dataType;

	/* package */ DataTypeMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		dataType = DataType.readDataType(bb);
	}

	public DataType getDataType() {
		return dataType;
	}

}
