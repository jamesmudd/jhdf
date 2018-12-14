package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.Superblock;

public class DataSpaceMessage extends Message {

	private final DataSpace dataSpace;

	public DataSpaceMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		dataSpace = DataSpace.readDataSpace(bb, sb);
	}
}
