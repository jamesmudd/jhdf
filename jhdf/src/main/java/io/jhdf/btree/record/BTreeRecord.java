/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree.record;

import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;

public abstract class BTreeRecord {

	@SuppressWarnings("unchecked") // Requires that the b-tree is of the correct type for the record
	public static <T extends BTreeRecord> T readRecord(byte type, ByteBuffer buffer) {
		switch (type) {
		case 5:
			return (T) new LinkNameForIndexedGroupRecord(buffer);
		case 8:
			return (T) new AttributeNameForIndexedAttributesRecord(buffer);
		default:
			throw new HdfException("Unknown b-tree record type. Type = " + type);
		}
	}

}
