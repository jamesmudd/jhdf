package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.Superblock;
import io.jhdf.Utils;

public class ObjectHeaderContinuationMessage extends Message {

	private final long offset;
	private final int lentgh;

	public ObjectHeaderContinuationMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		offset = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		lentgh = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfOffsets());
	}

	public long getOffset() {
		return offset;
	}

	public int getLentgh() {
		return lentgh;
	}
}
