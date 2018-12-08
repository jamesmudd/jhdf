package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class Message {

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

		return readMessage(headerData, sb, messageType);
	}

	private static Message readMessage(ByteBuffer bb, Superblock sb, int messageType) {
		switch (messageType) {
		case 0: // 0x000
			return new NilMessage(bb);
		case 1: // 0x001
			return new DataSpaceMessage(bb, sb);
		case 2: // 0x002
			return new LinkInfoMessage(bb, sb);
		case 3: // 0x003
			return new DataTypeMessage(bb);
		case 5: // 0x005
			return new FillValueMessage(bb, sb);
		case 6: // 0x006
			return new LinkMessage(bb, sb);
		case 8: // 0x008
			return new DataLayoutMessage(bb, sb);
		case 10: // 0x00A
			return new GroupInfoMessage(bb, sb);
		case 12: // 0x00C
			return new AttributeMessage(bb, sb);
		case 16: // 0x010
			return new ObjectHeaderContinuationMessage(bb, sb);
		case 17: // 0x011
			return new SymbolTableMessage(bb, sb);
		case 18: // 0x012
			return new ObjectModificationTimeMessage(bb);
		case 19: // 0x013
			return new BTreeKValuesMessage(bb, sb);
		case 22: // 0x016
			return new ObjectReferenceCountMessage(bb);

		default:
			throw new HdfException("Unreconized message type = " + messageType);
		}
	}

}
