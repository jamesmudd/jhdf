package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Superblock;
import io.jhdf.Utils;

/**
 * <p>
 * Object Header Continuation Message
 * </p>
 * 
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#ContinuationMessage">Format
 * Spec</a>
 * </p>
 * 
 * @author James Mudd
 */
public class ObjectHeaderContinuationMessage extends Message {

	private final long offset;
	private final int length;

	/* package */ ObjectHeaderContinuationMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		super(flags);

		offset = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		length = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfOffsets());
	}

	public long getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
}
