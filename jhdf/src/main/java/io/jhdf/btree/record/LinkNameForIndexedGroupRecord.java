package io.jhdf.btree.record;

import java.nio.ByteBuffer;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class LinkNameForIndexedGroupRecord extends BTreeRecord {

	private final long hash;
	private final ByteBuffer id;

	public LinkNameForIndexedGroupRecord(ByteBuffer bb) {
		if (bb.remaining() != 11) {
			throw new HdfException(
					"Invalid length buffer for LinkNameForIndexedGroupRecord. remaining bytes = " + bb.remaining());
		}

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
