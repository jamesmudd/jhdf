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

import io.jhdf.BufferBuilder;
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

	public static final int MESSAGE_TYPE = 10;

	private static final int LINK_PHASE_CHANGE_PRESENT = 0;
	private static final int ESTIMATED_ENTRY_INFORMATION_PRESENT = 1;

	private final byte version;
	private final BitSet flags;
	private final int maximumCompactLinks;
	private final int minimumDenseLinks;
	private final int estimatedNumberOfEntries;
	private final int estimatedLengthOfEntryName;

	/* package */ GroupInfoMessage(ByteBuffer bb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();
		if (version != 0) {
			throw new HdfException("Unrecognized version " + version);
		}

		flags = BitSet.valueOf(new byte[]{bb.get()});

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

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public ByteBuffer toBuffer() {
		BufferBuilder bufferBuilder = new BufferBuilder();
		bufferBuilder.writeByte(version);
		bufferBuilder.writeBitSet(flags, 1);
		if (flags.get(LINK_PHASE_CHANGE_PRESENT)) {
			bufferBuilder.writeShort(maximumCompactLinks);
			bufferBuilder.writeShort(minimumDenseLinks);
		}
		if (flags.get(ESTIMATED_ENTRY_INFORMATION_PRESENT)) {
			bufferBuilder.writeShort(estimatedNumberOfEntries);
			bufferBuilder.writeShort(estimatedLengthOfEntryName);
		}
		return bufferBuilder.build();
	}

	private GroupInfoMessage() {
		super(new BitSet(1));
		this.flags = new BitSet(1);
		this.version = 0;
		this.maximumCompactLinks = -1;
		this.minimumDenseLinks = -1;
		this.estimatedNumberOfEntries = -1;
		this. estimatedLengthOfEntryName = -1;
	}

	public static GroupInfoMessage createBasic() {
		return new GroupInfoMessage();
	}
}
