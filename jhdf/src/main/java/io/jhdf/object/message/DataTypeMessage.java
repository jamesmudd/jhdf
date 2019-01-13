package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.object.datatype.DataType;

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
