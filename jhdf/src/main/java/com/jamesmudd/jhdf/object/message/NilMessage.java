package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

public class NilMessage extends Message {

	public NilMessage(ByteBuffer bb) {
		super(bb);

		// Skip size
		bb.get(new byte[getDataSize()]);
	}

}
