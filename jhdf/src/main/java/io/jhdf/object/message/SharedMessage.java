/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;

/**
 * <p>
 * Shared Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#ObjectHeaderMessages">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class SharedMessage {
	/** Address of the object header containing the shared message */
	private final long objectHeaderAddress;

	public SharedMessage(ByteBuffer bb, Superblock sb) {
		final byte version = bb.get();
		if (version < 1 || version> 3) {
			throw new HdfException("Unrecognized shared message version " + version);
		}

		final byte type = bb.get();

		if(version == 1) {
			// Skip reserved bytes
			bb.position(bb.position() + 6);
		}

		if(version == 3 && type == 1) {
			throw new UnsupportedHdfException("Shared message v3 in fractal heap");
		}

		this.objectHeaderAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
	}

	public long getObjectHeaderAddress() {
		return objectHeaderAddress;
	}

}
