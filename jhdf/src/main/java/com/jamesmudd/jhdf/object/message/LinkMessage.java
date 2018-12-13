package com.jamesmudd.jhdf.object.message;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class LinkMessage extends Message {

	private static final int CREATION_ORDER_PRESENT = 2;
	private static final int LINK_TYPE_PRESENT = 3;
	private static final int LINK_CHARACTER_SET_PRESENT = 4;

	private final byte version;
	private final int linkType;
	private final long creationOrder;
	private final String linkName;
	private long hardLinkAddress;
	private String softLink;
	private String externalLink;

	public LinkMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		// Version
		version = bb.get();
		if (version != 1) {
			throw new HdfException("Unreconized version = " + version);
		}

		// Flags
		byte[] flagsBytes = new byte[] { bb.get() };
		BitSet flags = BitSet.valueOf(flagsBytes);

		// Size of length of link name
		final int sizeOfLentghOfLinkName;
		if (flags.get(1)) {
			if (flags.get(0)) {
				sizeOfLentghOfLinkName = 8;
			} else {
				sizeOfLentghOfLinkName = 4;
			}
		} else { // bit 0 = false
			if (flags.get(0)) {
				sizeOfLentghOfLinkName = 2;
			} else {
				sizeOfLentghOfLinkName = 1;
			}
		}

		if (flags.get(LINK_TYPE_PRESENT)) {
			linkType = Utils.readBytesAsUnsignedInt(bb, 1);
		} else {
			linkType = 0; // No link type specified so is hard link
		}

		if (flags.get(CREATION_ORDER_PRESENT)) {
			creationOrder = Utils.readBytesAsUnsignedLong(bb, 8);
		} else {
			creationOrder = -1;
		}

		final Charset linkNameCharset;
		if (flags.get(LINK_CHARACTER_SET_PRESENT)) {
			int charsetValue = Utils.readBytesAsUnsignedInt(bb, 1);
			switch (charsetValue) {
			case 0:
				linkNameCharset = US_ASCII;
				break;
			case 1:
				linkNameCharset = UTF_8;
				break;
			default:
				throw new HdfException("Unknown link charset value = " + charsetValue);
			}
		} else {
			linkNameCharset = US_ASCII;
		}

		final int lentghOfLinkName = Utils.readBytesAsUnsignedInt(bb, sizeOfLentghOfLinkName);

		ByteBuffer nameBuffer = Utils.createSubBuffer(bb, lentghOfLinkName);

		linkName = linkNameCharset.decode(nameBuffer).toString();

		// Link Information
		switch (linkType) {
		case 0: // Hard Link
			hardLinkAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			break;
		case 1: // Soft link
			int lentghOfSoftLink = Utils.readBytesAsUnsignedInt(bb, 2);
			ByteBuffer linkBuffer = Utils.createSubBuffer(bb, lentghOfSoftLink);
			softLink = US_ASCII.decode(linkBuffer).toString();
			break;
		case 64: // External link
			int lentghOfExternalLink = Utils.readBytesAsUnsignedInt(bb, 2);
			ByteBuffer externalLinkBuffer = Utils.createSubBuffer(bb, lentghOfExternalLink);
			// Skip first byte contains version = 0 and flags = 0
			externalLinkBuffer.position(1);
			externalLink = US_ASCII.decode(externalLinkBuffer).toString();
			break;
		default:
			throw new HdfException("Unreconized link type = " + linkType);
		}
	}

	public byte getVersion() {
		return version;
	}

	public int getLinkType() {
		return linkType;
	}

	public long getCreationOrder() {
		return creationOrder;
	}

	public String getLinkName() {
		return linkName;
	}

	public long getHardLinkAddress() {
		return hardLinkAddress;
	}

	public String getSoftLink() {
		return softLink;
	}

	public String getExternalLink() {
		return externalLink;
	}

}
