package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ObjectModificationTimeMessage extends Message {

	private final byte version;
	private final long unixEpocSecond;

	public ObjectModificationTimeMessage(ByteBuffer bb) {
		super(bb);

		version = bb.get();

		// Skip 3 unused bytes
		bb.get(new byte[3]);

		// Convert to unsigned long
		unixEpocSecond = Integer.toUnsignedLong(bb.getInt());
	}

	public LocalDateTime getModifiedTime() {
		return LocalDateTime.ofEpochSecond(unixEpocSecond, 0, ZoneOffset.UTC);
	}

	public byte getVersion() {
		return version;
	}

	public long getUnixEpocSecond() {
		return unixEpocSecond;
	}

}
