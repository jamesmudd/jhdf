package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

public class NilMessage extends Message {

	public NilMessage(ByteBuffer bb) {
		super(bb);
		// Move buffer to the end
		bb.position(bb.capacity());
	}

}
