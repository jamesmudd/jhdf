package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;

public class ObjectHeaderContinuationMessage extends Message {

	private final long offset;
	private final long lentgh;

	public ObjectHeaderContinuationMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		// FIXME should support variable lentgh and offset
		offset = bb.getLong();
		lentgh = bb.getLong();
	}

	public long getOffset() {
		return offset;
	}

	public long getLentgh() {
		return lentgh;
	}
}
