package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.exceptions.HdfException;

public class ObjectReferenceCountMessage extends Message {

	private final int referenceCount;

	public ObjectReferenceCountMessage(ByteBuffer bb) {
		super(bb);

		byte version = bb.get();
		if (version != 0) {
			throw new HdfException("Unreconized version = " + version);
		}

		referenceCount = bb.getInt();
	}

	public int getReferenceCount() {
		return referenceCount;
	}

}
