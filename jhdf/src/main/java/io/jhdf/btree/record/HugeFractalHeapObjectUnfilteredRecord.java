/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree.record;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;

public class HugeFractalHeapObjectUnfilteredRecord extends BTreeRecord {
	private final long hugeObjectAddress;
	private final long hugeObjectLength;
	private final long hugeObjectID;

	public HugeFractalHeapObjectUnfilteredRecord(ByteBuffer bb) {
		if (bb.remaining() < 24) {
			throw new HdfException("Invalid length buffer for "
				+ "HugeFractalHeapObjectUnfilteredRecord. remaining bytes = " + bb.remaining());
		}

		hugeObjectAddress = Utils.readBytesAsUnsignedLong(bb, 8);
		hugeObjectLength = Utils.readBytesAsUnsignedLong(bb, 8);
		hugeObjectID = Utils.readBytesAsUnsignedLong(bb, 8);
	}

	public long getAddress() {
		return this.hugeObjectAddress;
	}

	public long getLength() {
		return this.hugeObjectLength;
	}

	public long getId() {
		return this.hugeObjectID;
	}
}

