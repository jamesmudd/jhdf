package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class Message {
	private static final Logger logger = LoggerFactory.getLogger(Message.class);

	public Message(ByteBuffer bb) {
		// TODO Is this needed?
	}

	public static Message readObjectHeaderV1Message(ByteBuffer bb, Superblock sb) {
		Utils.seekBufferToNextMultipleOfEight(bb);

		int messageType = Utils.readBytesAsUnsignedInt(bb, 2);
		int dataSize = Utils.readBytesAsUnsignedInt(bb, 2);
		BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

		// Skip 3 reserved zerobytes
		bb.position(bb.position() + 3);

		// Create a new buffer holding this header data
		ByteBuffer headerData = Utils.createSubBuffer(bb, dataSize);

		return readMessage(headerData, sb, messageType);
	}

	public static Message readObjectHeaderV2Message(ByteBuffer bb, Superblock sb) {
		int messageType = Utils.readBytesAsUnsignedInt(bb, 1);
		int dataSize = Utils.readBytesAsUnsignedInt(bb, 2);
		BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

		// Create a new buffer holding this header data
		ByteBuffer headerData = Utils.createSubBuffer(bb, dataSize);

		Message message = readMessage(headerData, sb, messageType);
		logger.debug("Read message: {}", message);
		if (headerData.hasRemaining()) {
			logger.warn("After reading buffer still has {} bytes remaining", headerData.remaining());
		}

		return message;
	}

	private static Message readMessage(ByteBuffer bb, Superblock sb, int messageType) {
		switch (messageType) {
		case 0: // 0x0000
			return new NilMessage(bb);
		case 1: // 0x0001
			return new DataSpaceMessage(bb, sb);
		case 2: // 0x0002
			return new LinkInfoMessage(bb, sb);
		case 3: // 0x0003
			return new DataTypeMessage(bb);
		case 5: // 0x0005
			return new FillValueMessage(bb, sb);
		case 6: // 0x0006
			return new LinkMessage(bb, sb);
		case 8: // 0x0008
			return new DataLayoutMessage(bb, sb);
		case 10: // 0x000A
			return new GroupInfoMessage(bb, sb);
		case 12: // 0x000C
			return new AttributeMessage(bb, sb);
		case 16: // 0x0010
			return new ObjectHeaderContinuationMessage(bb, sb);
		case 17: // 0x0011
			return new SymbolTableMessage(bb, sb);
		case 18: // 0x0012
			return new ObjectModificationTimeMessage(bb);
		case 19: // 0x0013
			return new BTreeKValuesMessage(bb, sb);
		case 21: // 0x0015
			return new AttributeInfoMessage(bb, sb);
		case 22: // 0x0016
			return new ObjectReferenceCountMessage(bb);

		default:
			throw new HdfException("Unreconized message type = " + messageType);
		}
	}

}
