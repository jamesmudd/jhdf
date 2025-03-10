/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

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

	public static final int MESSAGE_TYPE = 13;

	private final String comment;

	/* package */ ObjectCommentMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		comment = Utils.readUntilNull(bb);
	}

	public String getComment() {
		return comment;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}


}
