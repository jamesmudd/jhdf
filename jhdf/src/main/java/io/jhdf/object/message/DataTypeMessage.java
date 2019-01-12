package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.object.datatype.DataType;

public class DataTypeMessage extends Message {

	private final DataType dataType;

	public DataTypeMessage(ByteBuffer bb) {
		super(bb);

		dataType = DataType.readDataType(bb);
	}

	public DataType getDataType() {
		return dataType;
	}

}
