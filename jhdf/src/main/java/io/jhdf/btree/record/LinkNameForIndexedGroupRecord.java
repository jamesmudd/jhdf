package io.jhdf.btree.record;

import java.nio.ByteBuffer;

import io.jhdf.Utils;

public class LinkNameForIndexedGroupRecord extends Record {

	private final long hash;
	private final long id;

	public LinkNameForIndexedGroupRecord(ByteBuffer bb) {
		hash = Utils.readBytesAsUnsignedLong(bb, 4);
		id = Utils.readBytesAsUnsignedLong(bb, 7);
	}

	public long getHash() {
		return hash;
	}

	public long getId() {
		return id;
	}

}
