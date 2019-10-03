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

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.BitSet;

/**
 * <p>
 * Object Modification Time Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#ModificationTimeMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class ObjectModificationTimeMessage extends Message {

	private final long unixEpocSecond;

	/* package */ ObjectModificationTimeMessage(ByteBuffer bb, BitSet flags) {
		super(flags);

		final byte version = bb.get();
		if (version != 1) {
			throw new HdfException("Unrecognized version " + version);
		}

		// Skip 3 unused bytes
		bb.position(bb.position() + 3);

		// Convert to unsigned long
		unixEpocSecond = Utils.readBytesAsUnsignedLong(bb, 4);
	}

	public LocalDateTime getModifiedTime() {
		return LocalDateTime.ofEpochSecond(unixEpocSecond, 0, ZoneOffset.UTC);
	}

	public long getUnixEpocSecond() {
		return unixEpocSecond;
	}

}
