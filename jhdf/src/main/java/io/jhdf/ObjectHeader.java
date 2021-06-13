/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.checksum.ChecksumUtils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.Message;
import io.jhdf.object.message.ObjectHeaderContinuationMessage;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.jhdf.Utils.readBytesAsUnsignedInt;

public abstract class ObjectHeader {
	private static final Logger logger = LoggerFactory.getLogger(ObjectHeader.class);

	/**
	 * The location of this Object header in the file
	 */
	private final long address;
	/**
	 * The messages contained in this object header
	 */
	protected final List<Message> messages = new ArrayList<>();

	public long getAddress() {
		return address;
	}

	public abstract int getVersion();

	public abstract boolean isAttributeCreationOrderTracked();

	public abstract boolean isAttributeCreationOrderIndexed();

	public List<Message> getMessages() {
		return messages;
	}

	protected ObjectHeader(long address) {
		this.address = address;
	}

	public <T extends Message> List<T> getMessagesOfType(Class<T> type) {
		return getMessages().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
	}

	public <T extends Message> boolean hasMessageOfType(Class<T> type) {
		return !getMessagesOfType(type).isEmpty();
	}

	public <T extends Message> T getMessageOfType(Class<T> type) {
		List<T> messagesOfType = getMessagesOfType(type);
		// Validate only one message exists
		if (messagesOfType.isEmpty()) {
			throw new HdfException("Requested message type '" + type.getSimpleName() + "' not present");
		}
		if (messagesOfType.size() > 1) {
			throw new HdfException("Requested message type '" + type.getSimpleName() + "' is not unique");
		}

		return messagesOfType.get(0);
	}

	public static class ObjectHeaderV1 extends ObjectHeader {

		/**
		 * version of the header
		 */
		private final byte version;
		/**
		 * Level of the node 0 = leaf
		 */
		private final int referenceCount;

		private ObjectHeaderV1(HdfBackingStorage hdfBackingStorage, long address) {
			super(address);

			try {
				ByteBuffer header = hdfBackingStorage.readBufferFromAddress(address, 12);

				// Version
				version = header.get();
				if (version != 1) {
					throw new HdfException("Invalid version detected. Version is = " + version);
				}

				// Skip reserved byte
				header.position(header.position() + 1);

				// Number of messages
				final int numberOfMessages = readBytesAsUnsignedInt(header, 2);

				// Reference Count
				referenceCount = readBytesAsUnsignedInt(header, 4);

				// Size of the messages
				int headerSize = readBytesAsUnsignedInt(header, 4);

				// 12 up to this point + 4 missed in format spec = 16
				address += 16;
				header = hdfBackingStorage.readBufferFromAddress(address, headerSize);

				readMessages(hdfBackingStorage, header, numberOfMessages);

				logger.debug("Read object header from address: {}", address);

			} catch (Exception e) {
				throw new HdfException("Failed to read object header at address: " + address, e);
			}
		}

		private void readMessages(HdfBackingStorage hdfBackingStorage, ByteBuffer bb, int numberOfMessages) {
			while (bb.remaining() > 4 && messages.size() < numberOfMessages) {
				Message m = Message.readObjectHeaderV1Message(bb, hdfBackingStorage);
				messages.add(m);

				if (m instanceof ObjectHeaderContinuationMessage) {
					ObjectHeaderContinuationMessage ohcm = (ObjectHeaderContinuationMessage) m;

					ByteBuffer continuationBuffer = hdfBackingStorage.readBufferFromAddress(ohcm.getOffset(), ohcm.getLength());

					readMessages(hdfBackingStorage, continuationBuffer, numberOfMessages);
				}
			}
		}

		@Override
		public int getVersion() {
			return version;
		}

		public int getReferenceCount() {
			return referenceCount;
		}

		@Override
		public boolean isAttributeCreationOrderTracked() {
			return false; // Not supported in v1 headers
		}

		@Override
		public boolean isAttributeCreationOrderIndexed() {
			return false; // Not supported in v1 headers
		}

	}

	/**
	 * The Object Header V2
	 * <p>
	 * <a href=
	 * "https://support.hdfgroup.org/HDF5/doc/H5.format.html#V2ObjectHeaderPrefix">V2ObjectHeaderPrefix</a>
	 */
	public static class ObjectHeaderV2 extends ObjectHeader {

		private static final byte[] OBJECT_HEADER_V2_SIGNATURE = "OHDR".getBytes(StandardCharsets.US_ASCII);
		private static final byte[] OBJECT_HEADER_V2_CONTINUATION_SIGNATURE = "OCHK".getBytes(StandardCharsets.US_ASCII);

		private static final int ATTRIBUTE_CREATION_ORDER_TRACKED = 2;
		private static final int ATTRIBUTE_CREATION_ORDER_INDEXED = 3;
		private static final int NUMBER_OF_ATTRIBUTES_PRESENT = 4;
		private static final int TIMESTAMPS_PRESENT = 5;

		/**
		 * Type of node. 0 = group, 1 = data
		 */
		private final byte version;

		private final long accessTime;
		private final long modificationTime;
		private final long changeTime;
		private final long birthTime;

		private final int maximumNumberOfCompactAttributes;
		private final int maximumNumberOfDenseAttributes;
		private final BitSet flags;

		private ObjectHeaderV2(HdfBackingStorage hdfBackingStorage, long address) {
			super(address);
			int headerSize = 0; // Keep track of the size for checksum

			try {
				ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, 6);
				address += 6;
				headerSize += 6;

				byte[] formatSignatureBytes = new byte[OBJECT_HEADER_V2_SIGNATURE.length];
				bb.get(formatSignatureBytes);

				// Verify signature
				if (!Arrays.equals(OBJECT_HEADER_V2_SIGNATURE, formatSignatureBytes)) {
					throw new HdfException("Object header v2 signature not matched");
				}

				// Version
				version = bb.get();

				if (version != 2) {
					throw new HdfException("Invalid version detected. Version is = " + version);
				}

				// Flags
				flags = BitSet.valueOf(new byte[]{bb.get()});

				// Size of chunk 0
				final byte sizeOfChunk0;
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
					bb = hdfBackingStorage.readBufferFromAddress(address, 16);
					address += 16;
					headerSize += 16;

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
					bb = hdfBackingStorage.readBufferFromAddress(address, 4);
					address += 4;
					headerSize += 4;

					maximumNumberOfCompactAttributes = readBytesAsUnsignedInt(bb, 2);
					maximumNumberOfDenseAttributes = readBytesAsUnsignedInt(bb, 2);
				} else {
					maximumNumberOfCompactAttributes = -1;
					maximumNumberOfDenseAttributes = -1;
				}

				bb = hdfBackingStorage.readBufferFromAddress(address, sizeOfChunk0);
				address += sizeOfChunk0;
				headerSize += sizeOfChunk0;

				int sizeOfMessages = readBytesAsUnsignedInt(bb, sizeOfChunk0);

				bb = hdfBackingStorage.readBufferFromAddress(address, sizeOfMessages);
				headerSize += sizeOfMessages;

				// There might be a gap at the end of the header of up to 4 bytes
				// message type (1_byte) + message size (2 bytes) + message flags (1 byte)
				readMessages(hdfBackingStorage, bb);

				// Checksum
				headerSize += 4;
				ByteBuffer fullHeaderBuffer = hdfBackingStorage.readBufferFromAddress(super.getAddress(), headerSize);
				ChecksumUtils.validateChecksum(fullHeaderBuffer);

				logger.debug("Read object header from address: {}", address);

			} catch (Exception e) {
				throw new HdfException("Failed to read object header at address: " + address, e);
			}
		}

		public ObjectHeaderV2(long address, List<Message> messages) {
			super(address);
			this.messages.addAll(messages);
			version = 2;

			accessTime = -1;
			modificationTime = -1;
			changeTime = -1;
			birthTime = -1;

			maximumNumberOfCompactAttributes = -1;
			maximumNumberOfDenseAttributes = -1;

			flags = new BitSet(8); // TODO make consistent with values
		}

		public ByteBuffer toBuffer() {
			return new BufferBuilder()
				.writeBytes(OBJECT_HEADER_V2_SIGNATURE)
				.writeByte(version)
				.writeBitSet(flags, 1)
				.build();
		}

		private void readMessages(HdfBackingStorage hdfBackingStorage, ByteBuffer bb) {
			while (bb.remaining() >= 8) {
				Message m = Message.readObjectHeaderV2Message(bb, hdfBackingStorage, this.isAttributeCreationOrderTracked());
				messages.add(m);

				if (m instanceof ObjectHeaderContinuationMessage) {
					ObjectHeaderContinuationMessage ohcm = (ObjectHeaderContinuationMessage) m;
					ByteBuffer continuationBuffer = hdfBackingStorage.readBufferFromAddress(ohcm.getOffset(), ohcm.getLength());

					// Verify continuation block signature
					byte[] continuationSignatureBytes = new byte[OBJECT_HEADER_V2_CONTINUATION_SIGNATURE.length];
					continuationBuffer.get(continuationSignatureBytes);
					if (!Arrays.equals(OBJECT_HEADER_V2_CONTINUATION_SIGNATURE, continuationSignatureBytes)) {
						throw new HdfException(
							"Object header continuation header not matched, at address: " + ohcm.getOffset());
					}

					// Recursively read messages
					readMessages(hdfBackingStorage, continuationBuffer);

					continuationBuffer.rewind();
					ChecksumUtils.validateChecksum(continuationBuffer);
				}
			}
		}

		@Override
		public int getVersion() {
			return version;
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

		@Override
		public boolean isAttributeCreationOrderTracked() {
			return flags.get(ATTRIBUTE_CREATION_ORDER_TRACKED);
		}

		@Override
		public boolean isAttributeCreationOrderIndexed() {
			return flags.get(ATTRIBUTE_CREATION_ORDER_INDEXED);
		}

	}

	public static ObjectHeader readObjectHeader(HdfBackingStorage hdfBackingStorage, long address) {
		ByteBuffer bb = hdfBackingStorage.readBufferFromAddress(address, 1);
		byte version = bb.get();
		if (version == 1) {
			return new ObjectHeaderV1(hdfBackingStorage, address);
		} else {
			return new ObjectHeaderV2(hdfBackingStorage, address);
		}
	}

	public static LazyInitializer<ObjectHeader> lazyReadObjectHeader(HdfBackingStorage hdfBackingStorage, long address) {
		logger.debug("Creating lazy object header at address: {}", address);
		return new LazyInitializer<ObjectHeader>() {

			@Override
			protected ObjectHeader initialize() {
				logger.debug("Lazy initializing object header at address: {}", address);
				return readObjectHeader(hdfBackingStorage, address);
			}

		};
	}

}
