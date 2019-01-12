package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class NilMessage extends Message {

	/* package */ NilMessage(ByteBuffer bb, BitSet flags) {
		super(flags);
		// Move buffer to the end
		bb.position(bb.capacity());
	}

}
