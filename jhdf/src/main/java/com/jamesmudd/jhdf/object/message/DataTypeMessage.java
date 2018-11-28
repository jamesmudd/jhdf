package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.object.datatype.DataType;

public class DataTypeMessage extends Message {

	private final DataType dataType;

	public DataTypeMessage(ByteBuffer bb) {
		super(bb);

		dataType = DataType.readDataType(bb);
	}

}
