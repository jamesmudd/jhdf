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
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Attribute Info Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#AinfoMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class AttributeInfoMessage extends Message {

	private static final int MAXIMUM_CREATION_INDEX_PRESENT = 0;
	private static final int ATTRIBUTE_CREATION_ORDER_PRESENT = 1;

	private final int maximumCreationIndex;
	private final long fractalHeapAddress;
	private final long attributeNameBTreeAddress;
	private final long attributeCreationOrderBTreeAddress;

	/* package */ AttributeInfoMessage(ByteBuffer bb, Superblock sb, BitSet messageFlags) {
		super(messageFlags);

		final byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unrecognized version " + version);
		}

		BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

		if (flags.get(MAXIMUM_CREATION_INDEX_PRESENT)) {
			maximumCreationIndex = Utils.readBytesAsUnsignedInt(bb, 2);
		} else {
			maximumCreationIndex = -1;
		}

		fractalHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		attributeNameBTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		if (flags.get(ATTRIBUTE_CREATION_ORDER_PRESENT)) {
			attributeCreationOrderBTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		} else {
			attributeCreationOrderBTreeAddress = -1;
		}
	}

	public int getMaximumCreationIndex() {
		return maximumCreationIndex;
	}

	public long getFractalHeapAddress() {
		return fractalHeapAddress;
	}

	public long getAttributeNameBTreeAddress() {
		return attributeNameBTreeAddress;
	}

	public long getAttributeCreationOrderBTreeAddress() {
		return attributeCreationOrderBTreeAddress;
	}

}
