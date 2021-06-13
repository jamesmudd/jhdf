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

import io.jhdf.BufferBuilder;
import io.jhdf.Superblock;
import io.jhdf.Utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * <p>
 * Symbol Table Message
 * </p>
 *
 * <p>
 * <a href=
 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#SymbolTableMessage">Format
 * Spec</a>
 * </p>
 *
 * @author James Mudd
 */
public class SymbolTableMessage extends Message {

	private final long bTreeAddress;
	private final long localHeapAddress;

	/* package */ SymbolTableMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		super(flags);

		bTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		localHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
	}


	public SymbolTableMessage(BitSet flags, long bTreeAddress, long localHeapAddress) {
		super(flags);
		this.bTreeAddress = bTreeAddress;
		this.localHeapAddress = localHeapAddress;
	}

	public ByteBuffer toBuffer() {
		return new BufferBuilder()
			.writeBytes(super.toBytes())
			.writeLong(bTreeAddress)
			.writeLong(localHeapAddress)
			.build();
	}

	public long getBTreeAddress() {
		return bTreeAddress;
	}

	public long getLocalHeapAddress() {
		return localHeapAddress;
	}

}
