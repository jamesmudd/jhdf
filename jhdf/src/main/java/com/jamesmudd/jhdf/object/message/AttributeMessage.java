package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.object.datatype.DataType;

public class AttributeMessage extends Message {

	private final byte version;
	private final String name;
	private final DataType dataType;
	private final DataSpace dataSpace;
	private final ByteBuffer data;

	public AttributeMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();

//		bb.get(); // Skip reserved
//		short nameSize = bb.getShort();
//		short dataTypeSize = bb.getShort();
//		short dataSpaceSize = bb.getShort();
		bb.get(new byte[7]); // Skip size bytes just reading using the types seems to work ok

		name = Utils.readUntilNull(bb);
		Utils.seekBufferToNextMultipleOfEight(bb);

		dataType = DataType.readDataType(bb);
		Utils.seekBufferToNextMultipleOfEight(bb);

		dataSpace = DataSpace.readDataSpace(bb, sb);
		Utils.seekBufferToNextMultipleOfEight(bb);

		int dataSize = dataSpace.getTotalLentgh() * dataType.getSize();
		data = bb.slice(); // Create a new buffer starting at the current pos
		data.limit(dataSize); // Limit the buffer to the datasize

		// Move the buffer past the data
		bb.position(bb.position() + dataSize);
	}

	public int getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public DataSpace getDataSpace() {
		return dataSpace;
	}

	public ByteBuffer getData() {
		return data;
	}

}
