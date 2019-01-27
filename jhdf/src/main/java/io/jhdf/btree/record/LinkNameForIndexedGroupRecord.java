package io.jhdf.btree.record;

import java.nio.ByteBuffer;

import io.jhdf.Utils;

public class LinkNameForIndexedGroupRecord extends BTreeRecord {

	private final long hash;
	private final ByteBuffer id;

	public LinkNameForIndexedGroupRecord(ByteBuffer bb) {
		hash = Utils.readBytesAsUnsignedLong(bb, 4);
		id = Utils.createSubBuffer(bb, 7);
	}

	public long getHash() {
		return hash;
	}

	public ByteBuffer getId() {
		return id;
	}

}
