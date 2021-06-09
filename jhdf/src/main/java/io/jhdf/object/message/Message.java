/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.storage.HdfBackingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class Message {
	private static final Logger logger = LoggerFactory.getLogger(Message.class);

	// Message flags
	private static final int MESSAGE_DATA_CONSTANT = 0;
	private static final int MESSAGE_SHARED = 1;
	private static final int MESSAGE_SHOULD_NOT_BE_SHARED = 2;
	private static final int FAIL_ON_UNKNOWN_MESSAGE_TYPE_WITH_WRITE = 3;
	private static final int SET_FLAG_ON_MODIFICATION_WITH_UNKNOWN_MESSAGE = 4;
	private static final int OBJECT_MODIFIED_WITHOUT_UNDERSTANDING_MESSAGE = 5;
	private static final int MESSAGE_CAN_BE_SHARED = 6;
	private static final int ALWAYS_FAIL_ON_UNKNOWN_MESSAGE_TYPE = 7;

	private final BitSet flags;

	public Message(BitSet flags) {
		this.flags = flags;
	}

	public static Message readObjectHeaderV1Message(ByteBuffer bb, HdfBackingStorage hdfBackingStorage) {
		Utils.seekBufferToNextMultipleOfEight(bb);

		int messageType = Utils.readBytesAsUnsignedInt(bb, 2);
		int dataSize = Utils.readBytesAsUnsignedInt(bb, 2);
		BitSet flags = BitSet.valueOf(new byte[]{bb.get()});

		// Skip 3 reserved zero bytes
		bb.position(bb.position() + 3);

		// Create a new buffer holding this header data
		final ByteBuffer headerData = Utils.createSubBuffer(bb, dataSize);

		final Message message = readMessage(headerData, hdfBackingStorage, messageType, flags);
		logger.debug("Read message: {}", message);
		if (headerData.remaining() > 7) {
			logger.warn("After reading message ({}) buffer still has {} bytes remaining",
				message.getClass().getSimpleName(), headerData.remaining());
		}

		return message;
	}

	public static Message readObjectHeaderV2Message(ByteBuffer bb, HdfBackingStorage hdfBackingStorage, boolean attributeCreationOrderTracked) {
		int messageType = Utils.readBytesAsUnsignedInt(bb, 1);
		int dataSize = Utils.readBytesAsUnsignedInt(bb, 2);
		BitSet flags = BitSet.valueOf(new byte[]{bb.get()});
		if (attributeCreationOrderTracked) {
			//skip creation order
			bb.getShort();
		}

		// Create a new buffer holding this header data
		final ByteBuffer headerData = Utils.createSubBuffer(bb, dataSize);

		final Message message = readMessage(headerData, hdfBackingStorage, messageType, flags);
		logger.debug("Read message: {}", message);
		if (headerData.hasRemaining()) {
			logger.warn("After reading message ({}) buffer still has {} bytes remaining",
				message.getClass().getSimpleName(), headerData.remaining());
		}

		return message;
	}

	private static Message readMessage(ByteBuffer bb, HdfBackingStorage hdfBackingStorage, int messageType, BitSet flags) {
		switch (messageType) {
			case 0: // 0x0000
				return new NilMessage(bb, flags);
			case 1: // 0x0001
				return new DataSpaceMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 2: // 0x0002
				return new LinkInfoMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 3: // 0x0003
				return new DataTypeMessage(bb, flags);
			case 4: // 0x0004
				return new FillValueOldMessage(bb, flags);
			case 5: // 0x0005
				return new FillValueMessage(bb, flags);
			case 6: // 0x0006
				return new LinkMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 7: // 0x0007
				throw new UnsupportedHdfException("Encountered External Data Files Message, this is not supported by jHDF");
			case 8: // 0x0008
				return DataLayoutMessage.createDataLayoutMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 9: // 0x0009
				throw new HdfException("Encountered Bogus message. Is this a valid HDF5 file?");
			case 10: // 0x000A
				return new GroupInfoMessage(bb, flags);
			case 11: // 0x000B
				return new FilterPipelineMessage(bb, flags);
			case 12: // 0x000C
				return new AttributeMessage(bb, hdfBackingStorage, flags);
			case 13: // 0x000D
				return new ObjectCommentMessage(bb, flags);
			case 14: // 0x000E
				return new OldObjectModificationTimeMessage(bb, flags);
			case 15: // 0x000F
				throw new UnsupportedHdfException("Encountered Shared Message Table Message, this is not supported by jHDF");
			case 16: // 0x0010
				return new ObjectHeaderContinuationMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 17: // 0x0011
				return new SymbolTableMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 18: // 0x0012
				return new ObjectModificationTimeMessage(bb, flags);
			case 19: // 0x0013
				return new BTreeKValuesMessage(bb, flags);
			case 20: // 0x0014
				throw new UnsupportedHdfException("Encountered Driver Info Message, this is not supported by jHDF");
			case 21: // 0x0015
				return new AttributeInfoMessage(bb, hdfBackingStorage.getSuperblock(), flags);
			case 22: // 0x0016
				return new ObjectReferenceCountMessage(bb, flags);

			default:
				throw new HdfException("Unrecognized message type = " + messageType);
		}
	}

	public boolean isMessageDataConstant() {
		return flags.get(MESSAGE_DATA_CONSTANT);
	}

	public boolean isMessageShared() {
		return flags.get(MESSAGE_SHARED);
	}

	public boolean isMessageNotShared() {
		return flags.get(MESSAGE_SHOULD_NOT_BE_SHARED);
	}

	public boolean isFailOnUnknownTypeWithWrite() {
		return flags.get(FAIL_ON_UNKNOWN_MESSAGE_TYPE_WITH_WRITE);
	}

	public boolean isFlagToBeSetOnUnknownType() {
		return flags.get(SET_FLAG_ON_MODIFICATION_WITH_UNKNOWN_MESSAGE);
	}

	public boolean isObjectModifiedWithoutUnderstandingOfThisMessage() {
		return flags.get(OBJECT_MODIFIED_WITHOUT_UNDERSTANDING_MESSAGE);
	}

	public boolean isMessageShareable() {
		return flags.get(MESSAGE_CAN_BE_SHARED);
	}

	public boolean isAlwaysFailOnUnknownType() {
		return flags.get(ALWAYS_FAIL_ON_UNKNOWN_MESSAGE_TYPE);
	}

}
