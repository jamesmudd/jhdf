package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;

public class GroupInfoMessage extends Message {

	private static final int LINK_PHASE_CHANGE_PRESENT = 0;
	private static final int ESTIMATED_ENTRY_INFOMATION_PRESENT = 0;

	private final byte version;
	private final int maximumCompactLinks;
	private final int minimumDenseLinks;
	private final int estimatedNumberOfEntries;
	private final int estimatedLentghOfEntryName;

	public GroupInfoMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();
		byte[] flagsBytes = new byte[] { bb.get() };
		BitSet flags = BitSet.valueOf(flagsBytes);

		if (flags.get(LINK_PHASE_CHANGE_PRESENT)) {
			maximumCompactLinks = Utils.readBytesAsUnsignedInt(bb, 2);
			minimumDenseLinks = Utils.readBytesAsUnsignedInt(bb, 2);
		} else {
			maximumCompactLinks = -1;
			minimumDenseLinks = -1;
		}

		if (flags.get(ESTIMATED_ENTRY_INFOMATION_PRESENT)) {
			estimatedNumberOfEntries = Utils.readBytesAsUnsignedInt(bb, 2);
			estimatedLentghOfEntryName = Utils.readBytesAsUnsignedInt(bb, 2);
		} else {
			estimatedNumberOfEntries = -1;
			estimatedLentghOfEntryName = -1;
		}
	}

}
