package io.jhdf.object.message;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

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
		final BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

		// Size of length of link name
		final int sizeOfLentghOfLinkNameIndex = Utils.bitsToInt(flags, 0, 2);
		final int sizeOfLentghOfLinkName;
		switch (sizeOfLentghOfLinkNameIndex) {
		case 0:
			sizeOfLentghOfLinkName = 1;
			break;
		case 1:
			sizeOfLentghOfLinkName = 2;
			break;
		case 2:
			sizeOfLentghOfLinkName = 4;
			break;
		case 3:
			sizeOfLentghOfLinkName = 8;
			break;
		default:
			throw new HdfException("Unreconized size of link name");
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
