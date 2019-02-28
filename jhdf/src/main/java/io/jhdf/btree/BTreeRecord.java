package io.jhdf.btree;

import java.nio.ByteBuffer;

import io.jhdf.exceptions.HdfException;

public abstract class BTreeRecord {

	public static BTreeRecord readRecord(byte type, ByteBuffer buffer) {
		switch (type) {
		case 5:
			return new LinkNameForIndexedGroupRecord(buffer);

		default:
			throw new HdfException("Unknown b-tree record type. Type = " + type);
		}
	}

}
