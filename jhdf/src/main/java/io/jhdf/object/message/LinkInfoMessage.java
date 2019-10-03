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
import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class LinkInfoMessage extends Message {

	private static final int CREATION_ORDER_TRACKED = 0;
	private static final int CREATION_ORDER_INDEXED = 1;

	private final byte version;
	private final long maximumCreationIndex;
	private final long fractalHeapAddress;
	private final long bTreeNameIndexAddress;
	private final long bTreeCreationOrderIndexAddress;
	private final BitSet flags;

	/* package */ LinkInfoMessage(ByteBuffer bb, Superblock sb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();
		flags = BitSet.valueOf(new byte[] { bb.get() });

		if (flags.get(CREATION_ORDER_TRACKED)) {
			maximumCreationIndex = Utils.readBytesAsUnsignedLong(bb, 8);
		} else {
			maximumCreationIndex = -1;
		}

		fractalHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		bTreeNameIndexAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		if (flags.get(CREATION_ORDER_INDEXED)) {
			bTreeCreationOrderIndexAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		} else {
			bTreeCreationOrderIndexAddress = -1;
		}
	}

	public int getVersion() {
		return version;
	}

	public long getMaximumCreationIndex() {
		return maximumCreationIndex;
	}

	public long getFractalHeapAddress() {
		return fractalHeapAddress;
	}

	public long getBTreeNameIndexAddress() {
		return bTreeNameIndexAddress;
	}

	public long getBTreeCreationOrderIndexAddress() {
		return bTreeCreationOrderIndexAddress;
	}

	public boolean isLinkCreationOrderTracked() {
		return flags.get(CREATION_ORDER_TRACKED);
	}
}
