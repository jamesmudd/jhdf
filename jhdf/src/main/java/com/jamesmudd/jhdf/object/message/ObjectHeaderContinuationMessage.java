package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;

public class ObjectHeaderContinuationMessage extends Message {

	private final long offset;
	private final long lentgh;

	public ObjectHeaderContinuationMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		offset = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		lentgh = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
	}

	public long getOffset() {
		return offset;
	}

	public long getLentgh() {
		return lentgh;
	}
}
