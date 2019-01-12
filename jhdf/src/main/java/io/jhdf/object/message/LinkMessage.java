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

	public enum LinkType {
		HARD, SOFT, EXTERNAL;

		private static LinkType fromInt(int typeInt) {
			switch (typeInt) {
			case 0:
				return HARD;
			case 1:
				return SOFT;
			case 64:
				return EXTERNAL;
			default:
				throw new HdfException("Unreconized link type: " + typeInt);
			}
		}
	}

	private static final int CREATION_ORDER_PRESENT = 2;
	private static final int LINK_TYPE_PRESENT = 3;
	private static final int LINK_CHARACTER_SET_PRESENT = 4;

	private final byte version;
	private final LinkType linkType;
	private final long creationOrder;
	private final String linkName;
	private long hardLinkAddress;
	private String softLink;
	private String externalFile;
	private String externalPath;

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
			linkType = LinkType.fromInt(Utils.readBytesAsUnsignedInt(bb, 1));
		} else {
			linkType = LinkType.HARD; // No link type specified so is hard link
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
		case HARD: // Hard Link
			hardLinkAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			break;
		case SOFT: // Soft link
			int lentghOfSoftLink = Utils.readBytesAsUnsignedInt(bb, 2);
			ByteBuffer linkBuffer = Utils.createSubBuffer(bb, lentghOfSoftLink);
			softLink = US_ASCII.decode(linkBuffer).toString();
			break;
		case EXTERNAL: // External link
			int lentghOfExternalLink = Utils.readBytesAsUnsignedInt(bb, 2);
			ByteBuffer externalLinkBuffer = Utils.createSubBuffer(bb, lentghOfExternalLink);
			// Skip first byte contains version = 0 and flags = 0
			externalLinkBuffer.position(1);
			externalFile = Utils.readUntilNull(externalLinkBuffer);
			externalPath = Utils.readUntilNull(externalLinkBuffer);
			break;
		default:
			throw new HdfException("Unreconized link type = " + linkType);
		}
	}

	public byte getVersion() {
		return version;
	}

	public LinkType getLinkType() {
		return linkType;
	}

	public long getCreationOrder() {
		return creationOrder;
	}

	public String getLinkName() {
		return linkName;
	}

	public long getHardLinkAddress() {
		if (linkType == LinkType.HARD) {
			return hardLinkAddress;
		} else {
			throw new HdfException("This link message is not a hard link. Link type is: " + linkType);
		}
	}

	public String getSoftLink() {
		if (linkType == LinkType.SOFT) {
			return softLink;
		} else {
			throw new HdfException("This link message is not a soft link. Link type is: " + linkType);
		}
	}

	public String getExternalFile() {
		if (linkType == LinkType.EXTERNAL) {
			return externalFile;
		} else {
			throw new HdfException("This link message is not a external link. Link type is: " + linkType);
		}
	}

	public String getExternalPath() {
		if (linkType == LinkType.EXTERNAL) {
			return externalPath;
		} else {
			throw new HdfException("This link message is not a external link. Link type is: " + linkType);
		}
	}
}
