package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;

public class SymbolTableMessage extends Message {

	private final long bTreeAddress;
	private final long localHeapAddress;

	public SymbolTableMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		bTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		localHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
	}

	public long getbTreeAddress() {
		return bTreeAddress;
	}

	public long getLocalHeapAddress() {
		return localHeapAddress;
	}

}
