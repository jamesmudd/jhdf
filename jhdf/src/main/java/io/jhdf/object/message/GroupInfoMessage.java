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
import java.util.BitSet;

/**
 * <p>
 * Group Info Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#GroupInfoMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class GroupInfoMessage extends Message {

	private static final int LINK_PHASE_CHANGE_PRESENT = 0;
	private static final int ESTIMATED_ENTRY_INFORMATION_PRESENT = 0;

	private final int maximumCompactLinks;
	private final int minimumDenseLinks;
	private final int estimatedNumberOfEntries;
	private final int estimatedLengthOfEntryName;

	/* package */ GroupInfoMessage(ByteBuffer bb, BitSet messageFlags) {
		super(messageFlags);

		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unrecognized version " + version);
		}

		BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

		if (flags.get(LINK_PHASE_CHANGE_PRESENT)) {
			maximumCompactLinks = Utils.readBytesAsUnsignedInt(bb, 2);
			minimumDenseLinks = Utils.readBytesAsUnsignedInt(bb, 2);
		} else {
			maximumCompactLinks = -1;
			minimumDenseLinks = -1;
		}

		if (flags.get(ESTIMATED_ENTRY_INFORMATION_PRESENT)) {
			estimatedNumberOfEntries = Utils.readBytesAsUnsignedInt(bb, 2);
			estimatedLengthOfEntryName = Utils.readBytesAsUnsignedInt(bb, 2);
		} else {
			estimatedNumberOfEntries = -1;
			estimatedLengthOfEntryName = -1;
		}
	}

	public int getMaximumCompactLinks() {
		return maximumCompactLinks;
	}

	public int getMinimumDenseLinks() {
		return minimumDenseLinks;
	}

	public int getEstimatedNumberOfEntries() {
		return estimatedNumberOfEntries;
	}

	public int getEstimatedLengthOfEntryName() {
		return estimatedLengthOfEntryName;
	}

}
