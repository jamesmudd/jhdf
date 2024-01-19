/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.BufferBuilder;
import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class LinkMessage extends Message {

	public static final int MESSAGE_TYPE = 6;

	public enum LinkType {
		HARD(0),
		SOFT(1),
		EXTERNAL(64);

		private final int value;

		LinkType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		private static final Map<Integer, LinkType> LOOKUP_MAP;
		static {
			LOOKUP_MAP = Arrays.stream(values())
				.collect(Collectors.toMap(LinkType::getValue, Function.identity()));
		}

		private static LinkType fromValue(int value) {
			LinkType linkType = LOOKUP_MAP.get(value);
			if (linkType == null) {
				throw new HdfException("Unrecognized link type: " + value);
			}
			return linkType;
		}
	}

	private static final int CREATION_ORDER_PRESENT = 2;
	private static final int LINK_TYPE_PRESENT = 3;
	private static final int LINK_CHARACTER_SET_PRESENT = 4;

	private final byte version;
	private final BitSet flags;
	private final LinkType linkType;
	private final long creationOrder;
	private final Charset linkNameCharset;
	private final String linkName;

	private long hardLinkAddress;
	private String softLink;
	private String externalFile;
	private String externalPath;

	public static LinkMessage fromBuffer(ByteBuffer bb, Superblock sb) {
		return new LinkMessage(bb, sb, null);
	}

	/* package */ LinkMessage(ByteBuffer bb, Superblock sb, BitSet messageFlags) {
		super(messageFlags);

		// Version
		version = bb.get();
		if (version != 1) {
			throw new HdfException("Unrecognized version = " + version);
		}

		// Flags
		flags = BitSet.valueOf(new byte[]{bb.get()});

		// Size of length of link name
		final int sizeOfLengthOfLinkName = getSizeOfLengthOfLinkName();

		if (flags.get(LINK_TYPE_PRESENT)) {
			linkType = LinkType.fromValue(Utils.readBytesAsUnsignedInt(bb, 1));
		} else {
			linkType = LinkType.HARD; // No link type specified so is hard link
		}

		if (flags.get(CREATION_ORDER_PRESENT)) {
			creationOrder = Utils.readBytesAsUnsignedLong(bb, 8);
		} else {
			creationOrder = -1;
		}


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

		final int lengthOfLinkName = Utils.readBytesAsUnsignedInt(bb, sizeOfLengthOfLinkName);

		ByteBuffer nameBuffer = Utils.createSubBuffer(bb, lengthOfLinkName);

		linkName = linkNameCharset.decode(nameBuffer).toString();

		// Link Information
		switch (linkType) {
			case HARD: // Hard Link
				hardLinkAddress = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
				break;
			case SOFT: // Soft link
				int lengthOfSoftLink = Utils.readBytesAsUnsignedInt(bb, 2);
				ByteBuffer linkBuffer = Utils.createSubBuffer(bb, lengthOfSoftLink);
				softLink = US_ASCII.decode(linkBuffer).toString();
				break;
			case EXTERNAL: // External link
				int lengthOfExternalLink = Utils.readBytesAsUnsignedInt(bb, 2);
				ByteBuffer externalLinkBuffer = Utils.createSubBuffer(bb, lengthOfExternalLink);
				// Skip first byte contains version = 0 and flags = 0
				externalLinkBuffer.position(1);
				externalFile = Utils.readUntilNull(externalLinkBuffer);
				externalPath = Utils.readUntilNull(externalLinkBuffer);
				break;
			default:
				throw new HdfException("Unrecognized link type = " + linkType);
		}
	}

	private LinkMessage(BitSet messageFlags, byte version, BitSet flags, LinkType linkType, long creationOrder, Charset linkNameCharset, String linkName, long hardLinkAddress) {
		super(messageFlags);
		this.version = version;
		this.flags = flags;
		this.linkType = linkType;
		this.creationOrder = creationOrder;
		this.linkNameCharset = linkNameCharset;
		this.linkName = linkName;
		this.hardLinkAddress = hardLinkAddress;
	}

	private int getSizeOfLengthOfLinkName() {
		final int sizeOfLengthOfLinkNameIndex = Utils.bitsToInt(flags, 0, 2);
		final int sizeOfLengthOfLinkName;
		switch (sizeOfLengthOfLinkNameIndex) {
			case 0:
				sizeOfLengthOfLinkName = 1;
				break;
			case 1:
				sizeOfLengthOfLinkName = 2;
				break;
			case 2:
				sizeOfLengthOfLinkName = 4;
				break;
			case 3:
				sizeOfLengthOfLinkName = 8;
				break;
			default:
				throw new HdfException("Unrecognized size of link name");
		}
		return sizeOfLengthOfLinkName;
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

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public ByteBuffer toBuffer() {
		BufferBuilder bufferBuilder = new BufferBuilder();
		bufferBuilder.writeByte(version);
		bufferBuilder.writeBitSet(flags, 1);
		if (flags.get(LINK_TYPE_PRESENT)){
			bufferBuilder.writeByte(linkType.getValue());
		}
		if (flags.get(CREATION_ORDER_PRESENT)) {
			bufferBuilder.writeLong(creationOrder);
		}
		if (flags.get(LINK_CHARACTER_SET_PRESENT) && (linkNameCharset.equals(UTF_8)))
				{bufferBuilder.writeByte(1);
		}
		byte[] encodedLinkName = linkName.getBytes(linkNameCharset);
		bufferBuilder.writeShortestRepresentation(encodedLinkName.length);
		bufferBuilder.writeBytes(encodedLinkName);

		// Link Information
		switch (linkType) {
			case HARD:
				bufferBuilder.writeLong(hardLinkAddress);
				break;
			case SOFT:
				byte[] softLinkBytes = softLink.getBytes(US_ASCII);
				bufferBuilder.writeShort(softLinkBytes.length);
				bufferBuilder.writeBytes(softLinkBytes);
				break;
			case EXTERNAL:
				throw new UnsupportedHdfException("Writing External link not supported");
		}

		return bufferBuilder.build();
	}

	public static LinkMessage create(String name, long address) {
		BitSet flags = new BitSet(1);
		flags.set(LINK_CHARACTER_SET_PRESENT);
		return new LinkMessage(
			new BitSet(1),
			(byte) 1,
			flags,
			LinkType.HARD,
			-1,
			UTF_8,
			name,
			address
		);
	}
}
