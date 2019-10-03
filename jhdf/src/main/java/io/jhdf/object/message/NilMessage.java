/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Nil (Null) Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#NILMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class NilMessage extends Message {

	/* package */ NilMessage(ByteBuffer bb, BitSet flags) {
		super(flags);
		// Move buffer to the end
		bb.position(bb.limit());
	}

}
