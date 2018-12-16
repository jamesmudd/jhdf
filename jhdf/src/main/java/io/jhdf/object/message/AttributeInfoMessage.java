package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Superblock;
import io.jhdf.Utils;

public class AttributeInfoMessage extends Message {

	private static final int MAXIMUM_CREATION_INDEX_PRESENT = 0;
	private static final int ATTRIBUTE_CREATION_ORDER_PRESENT = 1;

	private final byte version;
	private final int maximumCreationIndex;
	private final long fractalHeapAddress;
	private final long attributeNameBTreeAddress;
	private final long attributeCreationOrderBTreeAddress;

	public AttributeInfoMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();

		BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

		if (flags.get(MAXIMUM_CREATION_INDEX_PRESENT)) {
			maximumCreationIndex = Utils.readBytesAsUnsignedInt(bb, 2);
		} else {
			maximumCreationIndex = -1;
		}

		fractalHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		attributeNameBTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		if (flags.get(ATTRIBUTE_CREATION_ORDER_PRESENT)) {
			attributeCreationOrderBTreeAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		} else {
			attributeCreationOrderBTreeAddress = -1;
		}
	}

	public byte getVersion() {
		return version;
	}

	public int getMaximumCreationIndex() {
		return maximumCreationIndex;
	}

	public long getFractalHeapAddress() {
		return fractalHeapAddress;
	}

	public long getAttributeNameBTreeAddress() {
		return attributeNameBTreeAddress;
	}

	public long getAttributeCreationOrderBTreeAddress() {
		return attributeCreationOrderBTreeAddress;
	}

}
