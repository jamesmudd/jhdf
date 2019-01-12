package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Superblock;
import io.jhdf.Utils;

public class LinkInfoMessage extends Message {

	private static final int CREATION_ORDER_TRACKED = 0;
	private static final int CREATION_ORDER_INDEXED = 1;

	private final byte version;
	private final long maximumCreationIndex;
	private final long fractalHeapAddress;
	private final long bTreeNameIndexAddress;
	private final long bTreeCreationOrderIndexAddress;

	/* package */ LinkInfoMessage(ByteBuffer bb, Superblock sb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();
		byte[] flagsBytes = new byte[] { bb.get() };
		BitSet flags = BitSet.valueOf(flagsBytes);

		if (flags.get(CREATION_ORDER_TRACKED)) {
			maximumCreationIndex = Utils.readBytesAsUnsignedLong(bb, 8);
		} else {
			maximumCreationIndex = -1;
		}

		fractalHeapAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		bTreeNameIndexAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());

		if (flags.get(CREATION_ORDER_INDEXED)) {
			bTreeCreationOrderIndexAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		} else {
			bTreeCreationOrderIndexAddress = -1;
		}
	}

	public int getVersion() {
		return version;
	}

	public long getMaximumCreationIndex() {
		return maximumCreationIndex;
	}

	public long getFractalHeapAddress() {
		return fractalHeapAddress;
	}

	public long getbTreeNameIndexAddress() {
		return bTreeNameIndexAddress;
	}

	public long getbTreeCreationOrderIndexAddress() {
		return bTreeCreationOrderIndexAddress;
	}
}
