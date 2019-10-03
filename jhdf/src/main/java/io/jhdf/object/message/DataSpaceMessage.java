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

	private final DataSpace dataSpace;

	/* package */ DataSpaceMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		super(flags);

		dataSpace = DataSpace.readDataSpace(bb, sb);
	}

	public DataSpace getDataSpace() {
		return dataSpace;
	}
}
