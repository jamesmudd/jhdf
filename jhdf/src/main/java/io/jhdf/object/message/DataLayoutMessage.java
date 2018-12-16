package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

public class DataLayoutMessage extends Message {

	private final byte version;
	private final byte layoutClass;
	private final long address;
	private final long size;

	public DataLayoutMessage(ByteBuffer bb, Superblock sb) {
		super(bb);

		version = bb.get();

		if (version != 3 && version != 4) {
			throw new UnsupportedHdfException(
					"Only v3 and v4 data layout messages are supported. Detected version = " + version);
		}

		layoutClass = bb.get();

		switch (layoutClass) {
		case 0: // Compact Storage
			throw new UnsupportedHdfException("Compact storage is not supported");
		case 1: // Contiguous Storage
			address = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfOffsets());
			size = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfLengths());
			break;
		case 2: // Chunked Storage
			throw new UnsupportedHdfException("Chunked storage is not supported");
		case 3: // Virtual storage
			throw new UnsupportedHdfException("Virtual storage is not supported");
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
