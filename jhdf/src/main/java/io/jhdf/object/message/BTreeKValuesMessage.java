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

import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * B-tree ‘K’ Values Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#BtreeKValuesMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class BTreeKValuesMessage extends Message {

	private final short indexedStorageInternalNodeK;
	private final short groupInternalNodeK;
	private final short groupLeafNodeK;

	/* package */ BTreeKValuesMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unrecognized version " + version);
		}

		indexedStorageInternalNodeK = bb.getShort();
		groupInternalNodeK = bb.getShort();
		groupLeafNodeK = bb.getShort();
	}

	public short getIndexedStorageInternalNodeK() {
		return indexedStorageInternalNodeK;
	}

	public short getGroupInternalNodeK() {
		return groupInternalNodeK;
	}

	public short getGroupLeafNodeK() {
		return groupLeafNodeK;
	}

}
