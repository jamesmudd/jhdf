package io.jhdf.btree.record;

import java.nio.ByteBuffer;

import io.jhdf.exceptions.HdfException;

public abstract class Record {

	public static Record readRecord(byte type, ByteBuffer buffer) {
		switch (type) {
		case 5:
			return new LinkNameForIndexedGroupRecord(buffer);

		default:
			throw new HdfException("Unknow record type. Type = " + type);
		}
	}

}
