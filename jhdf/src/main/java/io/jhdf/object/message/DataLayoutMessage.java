package io.jhdf.object.message;

import java.nio.ByteBuffer;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

public abstract class DataLayoutMessage extends Message {

	public abstract DataLayout getDataLayout();

	public static DataLayoutMessage createDataLayoutMessage(ByteBuffer bb, Superblock sb) {
		final byte version = bb.get();

		if (version != 3 && version != 4) {
			throw new UnsupportedHdfException(
					"Only v3 and v4 data layout messages are supported. Detected version = " + version);
		}

		final byte layoutClass = bb.get();

		switch (layoutClass) {
		case 0: // Compact Storage
			return new CompactDataLayoutMessage(bb, sb);
		case 1: // Contiguous Storage
			return new ContigiousDataLayoutMessage(bb, sb);
		case 2: // Chunked Storage
			return new ChunkedDataLayoutMessage(bb, sb);
		case 3: // Virtual storage
			throw new UnsupportedHdfException("Virtual storage is not supported");
		default:
			throw new UnsupportedHdfException("Unknown storage layout " + layoutClass);
		}
	}

	public static class CompactDataLayoutMessage extends DataLayoutMessage {

		private final ByteBuffer dataBuffer;

		private CompactDataLayoutMessage(ByteBuffer bb, Superblock sb) {
			final int compactDataSize = Utils.readBytesAsUnsignedInt(bb, 2);
			dataBuffer = Utils.createSubBuffer(bb, compactDataSize);
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.COMPACT;
		}

		public ByteBuffer getDataBuffer() {
			return dataBuffer;
		}
	}

	public static class ContigiousDataLayoutMessage extends DataLayoutMessage {

		private final long address;
		private final long size;

		private ContigiousDataLayoutMessage(ByteBuffer bb, Superblock sb) {
			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			size = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CONTIGUOUS;
		}

		public long getAddress() {
			return address;
		}

		public long getSize() {
			return size;
		}
	}

	public static class ChunkedDataLayoutMessage extends DataLayoutMessage {

		private final long address;
		private final long size;

		private ChunkedDataLayoutMessage(ByteBuffer bb, Superblock sb) {
			// Not sure why this needs -1 but seems to be the way its done
			int chunkDimensionality = Utils.readBytesAsUnsignedInt(bb, 1) - 1;
			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
			long[] dimSizes = new long[chunkDimensionality];
			for (int i = 0; i < dimSizes.length; i++) {
				dimSizes[i] = Utils.readBytesAsUnsignedLong(bb, 4);
			}
			size = Utils.readBytesAsUnsignedLong(bb, 4);
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CHUNKED;
		}

		public long getAddress() {
			return address;
		}

		public long getSize() {
			return size;
		}
	}

}
