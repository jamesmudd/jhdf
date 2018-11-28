package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;

public class FillValueMessage extends Message {

	private final byte version;
	private final byte spaceAllocationTime;
	private final byte fillValueWriteTime;
	private final boolean fillValueDefined;
	private final ByteBuffer fillValue;

	public FillValueMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();
		spaceAllocationTime = bb.get();
		fillValueWriteTime = bb.get();
		fillValueDefined = bb.get() == 1;

		if (version == 2 && fillValueDefined) {
			int size = bb.getInt();
			fillValue = bb.slice();
			fillValue.limit(size);
			fillValue.order(bb.order());
			fillValue.rewind();
		} else {
			fillValue = null; // No fill value defined
		}
	}

	public boolean isFillValueDefined() {
		return fillValueDefined;
	}

	public byte getSpaceAllocationTime() {
		return spaceAllocationTime;
	}

	public byte getFillValueWriteTime() {
		return fillValueWriteTime;
	}
}
