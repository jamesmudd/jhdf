package com.jamesmudd.jhdf;

import static com.jamesmudd.jhdf.Utils.toHex;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmudd.jhdf.exceptions.HdfException;
import com.jamesmudd.jhdf.object.message.Message;
import com.jamesmudd.jhdf.object.message.ObjectHeaderContinuationMessage;

public abstract class ObjectHeader {
	private static final Logger logger = LoggerFactory.getLogger(ObjectHeader.class);

	public static ObjectHeader readObjectHeader(FileChannel fc, Superblock sb, long address) {
		ByteBuffer bb = ByteBuffer.allocate(1);
		try {
			fc.read(bb, address);
		} catch (IOException e) {
			throw new HdfException("Failed to read object header at address = " + toHex(address));
		}
		bb.rewind();
		byte version = bb.get();
		if (version == 1) {
			return new ObjectHeaderV1(fc, sb, address);
		} else {
			return new ObjectHeaderV2(fc, sb, address);
		}
	}

	public abstract long getAddress();

	public abstract int getVersion();

	public abstract List<Message> getMessages();

	public <T> List<T> getMessagesOfType(Class<T> type) {
		return getMessages().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
	}

	public static class ObjectHeaderV1 extends ObjectHeader {

		/** The location of this B tree in the file */
		private final long address;
		/** Type of node. 0 = group, 1 = data */
		private final byte version;
		/** Level of the node 0 = leaf */
		private final int referenceCount;
		/** The messages contained in this object header */
		private final List<Message> messages;

		private ObjectHeaderV1(FileChannel fc, Superblock sb, long address) {
			this.address = address;

			try {
				ByteBuffer header = ByteBuffer.allocate(12);
				fc.read(header, address);
				header.order(LITTLE_ENDIAN);
				header.rewind();

				// Version
				version = header.get();

				// Skip reserved byte
				header.get();

				// Number of messages
				short numberOfMessages = header.getShort();
				messages = new ArrayList<>(numberOfMessages);

				// Reference Count
				referenceCount = header.getInt();

				// Size of the messages
				int headerSize = header.getInt();

				header = ByteBuffer.allocate(headerSize);
				fc.read(header, address + 12 // Upto this point
						+ 4); // Padding missed in format spec);
				header.order(LITTLE_ENDIAN);
				header.rewind();

				for (int i = 0; i < numberOfMessages; i++) {
					Message m = Message.readObjectHeaderV1Message(header, sb);
					messages.add(m);
					if (m instanceof ObjectHeaderContinuationMessage) {
						ObjectHeaderContinuationMessage ohcm = (ObjectHeaderContinuationMessage) m;
						header = ByteBuffer.allocate(ohcm.getLentgh());
						fc.read(header, ohcm.getOffset());
						header.order(LITTLE_ENDIAN);
						header.rewind();
					}
				}

				logger.debug("Read object header from: {}", Utils.toHex(address));

			} catch (Exception e) {
				throw new HdfException("Failed to read object header at: " + Utils.toHex(address), e);
			}
		}

		@Override
		public long getAddress() {
			return address;
		}

		@Override
		public int getVersion() {
			return version;
		}

		public int getReferenceCount() {
			return referenceCount;
		}

		@Override
		public List<Message> getMessages() {
			return messages;
		}
	}

	/**
	 * The Object Header V2
	 * 
	 * <a href=
	 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#V2ObjectHeaderPrefix">V2ObjectHeaderPrefix</a>
	 */
	public static class ObjectHeaderV2 extends ObjectHeader {

		private static final byte[] OBJECT_HEADER_V2_SIGNATURE = "OHDR".getBytes();

		private static final int ATTRIBUTE_CREATION_ORDER_TRACKED = 2;
		private static final int ATTRIBUTE_CREATION_ORDER_INDEXED = 3;
		private static final int NUMBER_OF_ATTRIBUTES_PRESENT = 4;
		private static final int TIMESTAMPS_PRESENT = 5;

		/** The location of this B tree in the file */
		private final long address;
		/** Type of node. 0 = group, 1 = data */
		private final byte version;

		private final byte sizeOfChunk0;

		private final long accessTime;
		private final long modificationTime;
		private final long changeTime;
		private final long birthTime;

		private final int maximumNumberOfCompactAttributes;
		private final int maximumNumberOfDenseAttributes;

		/** The messages contained in this object header */
		private final List<Message> messages = new ArrayList<>();

		private ObjectHeaderV2(FileChannel fc, Superblock sb, long address) {
			this.address = address;

			try {
				ByteBuffer bb = ByteBuffer.allocate(6);
				fc.read(bb, address);
				address += 6;
				bb.order(LITTLE_ENDIAN);
				bb.rewind();

				byte[] formatSignitureByte = new byte[OBJECT_HEADER_V2_SIGNATURE.length];
				bb.get(formatSignitureByte);

				// Verify signature
				if (!Arrays.equals(OBJECT_HEADER_V2_SIGNATURE, formatSignitureByte)) {
					throw new HdfException("Heap signature not matched");
				}

				// Version
				version = bb.get();

				if (version != 2) {
					throw new HdfException("Invalid version detected. Version is = " + version);
				}

				// Flags
				byte[] flagsBytes = new byte[] { bb.get() };
				BitSet flags = BitSet.valueOf(flagsBytes);

				// Size of chunk 0
				if (flags.get(1)) {
					if (flags.get(0)) {
						sizeOfChunk0 = 8;
					} else {
						sizeOfChunk0 = 4;
					}
				} else { // bit 0 = false
					if (flags.get(0)) {
						sizeOfChunk0 = 2;
					} else {
						sizeOfChunk0 = 1;
					}
				}

				// Timestamps
				if (flags.get(TIMESTAMPS_PRESENT)) {
					bb = ByteBuffer.allocate(16);
					fc.read(bb, address);
					address += 16;
					bb.order(LITTLE_ENDIAN);
					bb.rewind();

					accessTime = Utils.readBytesAsUnsignedLong(bb, 4);
					modificationTime = Utils.readBytesAsUnsignedLong(bb, 4);
					changeTime = Utils.readBytesAsUnsignedLong(bb, 4);
					birthTime = Utils.readBytesAsUnsignedLong(bb, 4);
				} else {
					accessTime = -1;
					modificationTime = -1;
					changeTime = -1;
					birthTime = -1;
				}

				// Number of attributes
				if (flags.get(NUMBER_OF_ATTRIBUTES_PRESENT)) {
					bb = ByteBuffer.allocate(4);
					fc.read(bb, address);
					address += 4;
					bb.order(LITTLE_ENDIAN);
					bb.rewind();

					maximumNumberOfCompactAttributes = Utils.readBytesAsUnsignedInt(bb, 2);
					maximumNumberOfDenseAttributes = Utils.readBytesAsUnsignedInt(bb, 2);
				} else {
					maximumNumberOfCompactAttributes = -1;
					maximumNumberOfDenseAttributes = -1;
				}

				bb = ByteBuffer.allocate(sizeOfChunk0);
				fc.read(bb, address);
				address += sizeOfChunk0;
				bb.order(LITTLE_ENDIAN);
				bb.rewind();

				int sizeOfMessages = Utils.readBytesAsUnsignedInt(bb, sizeOfChunk0);

				bb = ByteBuffer.allocate(sizeOfMessages);
				fc.read(bb, address);
				bb.order(LITTLE_ENDIAN);
				bb.rewind();

				// There might be a gap at the end of the header of upto 4 bytes
				// message type (1_byte) + message size (2 bytes) + message flags (1 byte)
				bb = readMessages(fc, sb, bb);

				logger.debug("Read object header from: {}", Utils.toHex(address));

			} catch (Exception e) {
				throw new HdfException("Failed to read object header at: " + Utils.toHex(address), e);
			}
		}

		private ByteBuffer readMessages(FileChannel fc, Superblock sb, ByteBuffer bb) throws IOException {
			while (bb.remaining() > 4) {
				Message m = Message.readObjectHeaderV2Message(bb, sb);
				messages.add(m);

				if (m instanceof ObjectHeaderContinuationMessage) {
					ObjectHeaderContinuationMessage ohcm = (ObjectHeaderContinuationMessage) m;
					ByteBuffer continuationBuffer = ByteBuffer.allocate(ohcm.getLentgh());
					fc.read(continuationBuffer, ohcm.getOffset());
					continuationBuffer.order(LITTLE_ENDIAN);

					// Its a V2 Continuation block so skip the header
					// TODO should check the 'OCHK' signature might need to refactor...
					continuationBuffer.position(4);
					readMessages(fc, sb, continuationBuffer);
				}
			}
			return bb;
		}

		@Override
		public long getAddress() {
			return address;
		}

		@Override
		public int getVersion() {
			return version;
		}

		@Override
		public List<Message> getMessages() {
			return messages;
		}

		public long getAccessTime() {
			return accessTime;
		}

		public long getModificationTime() {
			return modificationTime;
		}

		public long getChangeTime() {
			return changeTime;
		}

		public long getBirthTime() {
			return birthTime;
		}

		public int getMaximumNumberOfCompactAttributes() {
			return maximumNumberOfCompactAttributes;
		}

		public int getMaximumNumberOfDenseAttributes() {
			return maximumNumberOfDenseAttributes;
		}

	}
}
