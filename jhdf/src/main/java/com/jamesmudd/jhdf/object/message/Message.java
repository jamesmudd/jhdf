package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class Message {

	private final short messageType;
	private final short dataSize;
	private final BitSet flags;

	public Message(ByteBuffer bb) {
		messageType = bb.getShort();
		dataSize = bb.getShort();
//		logger.debug("messageType = {}, dataSize = {}", messageType, dataSize);
		flags = BitSet.valueOf(new byte[] { bb.get() });

		// Skip 3 reserved zerobytes
		bb.get(new byte[3]);
	}

	public static Message readMessage(ByteBuffer bb, Superblock sb) {
		Utils.seekBufferToNextMultipleOfEight(bb);
		bb.mark();
		int messageType = bb.getShort();
		bb.reset(); // Move back to before reading the message type

		switch (messageType) {
		case 0: // 0x0000
			return new NilMessage(bb);
		case 1: // 0x0001
			return new DataSpaceMessage(bb, sb);
		case 3: // 0x0003
			return new DataTypeMessage(bb);
		case 5: // 0x0005
			return new FillValueMessage(bb, sb);
		case 8: // 0x0008
			return new DataLayoutMessage(bb, sb);
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
		case 22: // 0x0016
			return new ObjectReferenceCountMessage(bb);

		default:
			throw new HdfException("Unreconized message type = " + messageType);
		}
	}

	public short getMessageType() {
		return messageType;
	}

	public short getDataSize() {
		return dataSize;
	}

	public BitSet getFlags() {
		return flags;
	}

}
