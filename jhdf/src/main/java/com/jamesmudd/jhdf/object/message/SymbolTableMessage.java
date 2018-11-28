package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;

public class SymbolTableMessage extends Message {

	private final long bTreeAddress;
	private final long localHeapAddress;

	public SymbolTableMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		// FIXME variable offset/lentghs
		bTreeAddress = bb.getLong();
		localHeapAddress = bb.getLong();
	}
}
