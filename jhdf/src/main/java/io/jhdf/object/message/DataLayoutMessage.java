package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

public abstract class DataLayoutMessage extends Message {

	public DataLayoutMessage(BitSet flags) {
		super(flags);
	}

	public abstract DataLayout getDataLayout();

	public static DataLayoutMessage createDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
		final byte version = bb.get();

		if (version != 3 && version != 4) {
			throw new UnsupportedHdfException(
					"Only v3 and v4 data layout messages are supported. Detected version = " + version);
		}

		final byte layoutClass = bb.get();

		switch (layoutClass) {
		case 0: // Compact Storage
			return new CompactDataLayoutMessage(bb, flags);
		case 1: // Contiguous Storage
			return new ContigiousDataLayoutMessage(bb, sb, flags);
		case 2: // Chunked Storage
			if (version == 3) {
				return new ChunkedDataLayoutMessageV3(bb, sb, flags);
			} else { // v4
				return new ChunkedDataLayoutMessageV4(bb, sb, flags);
			}
		case 3: // Virtual storage
			throw new UnsupportedHdfException("Virtual storage is not supported");
		default:
			throw new UnsupportedHdfException("Unknown storage layout " + layoutClass);
		}
	}

	public static class CompactDataLayoutMessage extends DataLayoutMessage {

		private final ByteBuffer dataBuffer;

		private CompactDataLayoutMessage(ByteBuffer bb, BitSet flags) {
			super(flags);
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

		private ContigiousDataLayoutMessage(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);
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

	public static class ChunkedDataLayoutMessageV3 extends DataLayoutMessage {

		private final long address;
		private final long size;

		private ChunkedDataLayoutMessageV3(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);
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

	public static class ChunkedDataLayoutMessageV4 extends DataLayoutMessage {

		private final long address;
		private final int indexingType;

		private ChunkedDataLayoutMessageV4(ByteBuffer bb, Superblock sb, BitSet flags) {
			super(flags);

			final BitSet chunkedFlags = BitSet.valueOf(new byte[] { bb.get() });
			// Not sure why this needs -1 but seems to be the way its done
			final int chunkDimensionality = Utils.readBytesAsUnsignedInt(bb, 1) - 1;
			final int dimSizeBytes = Utils.readBytesAsUnsignedInt(bb, 1);

			long[] dimSizes = new long[chunkDimensionality];
			for (int i = 0; i < dimSizes.length; i++) {
				dimSizes[i] = Utils.readBytesAsUnsignedLong(bb, dimSizeBytes);
			}

			indexingType = Utils.readBytesAsUnsignedInt(bb, 1);

			address = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfOffsets());
		}

		@Override
		public DataLayout getDataLayout() {
			return DataLayout.CHUNKED;
		}

		public long getAddress() {
			return address;
		}

	}

}
