package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.Superblock;

public class BTreeKValuesMessage extends Message {

	private final byte version;
	private final short indexedStorageInternalNodeK;
	private final short groupInternalNodeK;
	private final short groupLeafNodeK;

	public BTreeKValuesMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();
		indexedStorageInternalNodeK = bb.getShort();
		groupInternalNodeK = bb.getShort();
		groupLeafNodeK = bb.getShort();
	}

}
