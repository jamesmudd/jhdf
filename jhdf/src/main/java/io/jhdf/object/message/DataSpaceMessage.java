/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Data Space Message. Used to describe the dimensionality of datasets.
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#DataspaceMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class DataSpaceMessage extends Message {

	public static final int MESSAGE_TYPE = 1;

	private final DataSpace dataSpace;

	/* package */ DataSpaceMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		super(flags);

		dataSpace = DataSpace.readDataSpace(bb, sb);
	}

	public DataSpace getDataSpace() {
		return dataSpace;
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public ByteBuffer toBuffer() {
		return null;
	}
}
