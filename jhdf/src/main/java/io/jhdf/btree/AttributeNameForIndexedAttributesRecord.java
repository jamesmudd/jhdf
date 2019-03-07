package io.jhdf.btree;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class AttributeNameForIndexedAttributesRecord extends BTreeRecord {

	private final ByteBuffer heapId;
	private final BitSet flags;
	private final long creationOrder;
	private final long hash;

	public AttributeNameForIndexedAttributesRecord(ByteBuffer bb) {
		if (bb.remaining() != 17) {
			throw new HdfException(
					"Invalid length buffer for AttributeNameForIndexedAttributesRecord. remaining bytes = "
							+ bb.remaining());
		}

		heapId = Utils.createSubBuffer(bb, 8);
		flags = BitSet.valueOf(new byte[] { bb.get() });
		creationOrder = Utils.readBytesAsUnsignedLong(bb, 4);
		hash = Utils.readBytesAsUnsignedLong(bb, 4);
	}

	public ByteBuffer getHeapId() {
		return heapId;
	}

	public BitSet getFlags() {
		return flags;
	}

	public long getCreationOrder() {
		return creationOrder;
	}

	public long getHash() {
		return hash;
	}

}
