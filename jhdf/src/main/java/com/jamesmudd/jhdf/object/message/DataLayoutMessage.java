package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.exceptions.UnsupportedHdfException;

public class DataLayoutMessage extends Message {

	private final byte version;
	private final byte layoutClass;
	private final long address;
	private final long size;

	public DataLayoutMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();

		if (version != 3) {
			throw new UnsupportedHdfException("Onlt v3 data layout messages are supported");
		}

		layoutClass = bb.get();

		switch (layoutClass) {
		case 0: // Compact Storage
			throw new UnsupportedHdfException("Compact storage is not supported");
		case 1: // Contiguous Storage
			address = Utils.readBytesAsInt(bb, sb.getSizeOfOffsets());
			size = Utils.readBytesAsInt(bb, sb.getSizeOfLengths());
			break;
		case 2: // Chunked Storage
			throw new UnsupportedHdfException("Chunked storage is not supported");
		default:
			throw new UnsupportedHdfException("Unknown storage layout " + layoutClass);
		}
	}

	public long getAddress() {
		return address;
	}

	public long getSize() {
		return size;
	}

}
