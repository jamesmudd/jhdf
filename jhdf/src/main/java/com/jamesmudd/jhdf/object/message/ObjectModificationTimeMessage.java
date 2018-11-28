package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ObjectModificationTimeMessage extends Message {

	private final byte version;
	private final long unixEpocTime;

	public ObjectModificationTimeMessage(ByteBuffer bb) {
		super(bb);

		version = bb.get();

		// Skip 3 unused bytes
		bb.get(new byte[3]);

		// Convert to unsigned int
		unixEpocTime = (bb.getInt() & 0xffffffffL);
	}

	public LocalDateTime getModifiedTime() {
		return LocalDateTime.ofEpochSecond(unixEpocTime, 0, ZoneOffset.UTC);
	}
}
