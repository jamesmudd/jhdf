package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Utils;

/**
 * <p>
 * Object Comment Message
 * </p>
 * 
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#CommentMessage">Format
 * Spec</a>
 * </p>
 * 
 * @author James Mudd
 */
public class ObjectCommentMessage extends Message {

	private final String comment;

	/* package */ ObjectCommentMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		comment = Utils.readUntilNull(bb);
	}

	public String getComment() {
		return comment;
	}

}
