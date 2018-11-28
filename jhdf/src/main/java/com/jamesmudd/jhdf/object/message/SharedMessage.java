package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class SharedMessage {

	/** The location of this message in the file */
	private final long address;
	/** Type of shared message version */
	private final short version;
	/** Type of shared message. 0 = group, 1 = data */
	private final short type;
	/** Level of the node 0 = leaf */
	private final long messageAddress;

	public SharedMessage(FileChannel fc, long address, int sizeOfOffsets) {
		this.address = address;
		try {

			ByteBuffer bb = ByteBuffer.allocate(2);
			fc.read(bb, address);
			bb.rewind();

			version = bb.get();
			type = bb.get();

			switch (version) {
			case 1:
				bb = ByteBuffer.allocate(sizeOfOffsets);
				fc.read(bb, address + 8);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				messageAddress = bb.getLong();
				break;

			case 2:
				bb = ByteBuffer.allocate(sizeOfOffsets);
				fc.read(bb, address + 2);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				messageAddress = bb.getLong();
				break;

			case 3:
				bb = ByteBuffer.allocate(sizeOfOffsets);
				fc.read(bb, address + 2);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				messageAddress = bb.getLong();
				break;

			default:
				throw new HdfException("Unkown shared message version: " + version);
			}

		} catch (Exception e) {
			throw new HdfException("Error readng shared message header at: " + Utils.toHex(address));
		}
	}
}
