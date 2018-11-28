package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;

public class DataSpaceMessage extends Message {

	private final DataSpace dataSpace;

	public DataSpaceMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		dataSpace = DataSpace.readDataSpace(bb, sb);
	}
}
