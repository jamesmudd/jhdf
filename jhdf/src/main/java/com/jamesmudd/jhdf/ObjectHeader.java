package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmudd.jhdf.exceptions.HdfException;
import com.jamesmudd.jhdf.object.message.Message;
import com.jamesmudd.jhdf.object.message.ObjectHeaderContinuationMessage;

public abstract class ObjectHeader {
	private static final Logger logger = LoggerFactory.getLogger(ObjectHeader.class);

	public static ObjectHeader readObjectHeader(FileChannel fc, Superblock sb, long address) {
		return new ObjectHeaderV1(fc, sb, address);
	}

	public abstract long getAddress();

	public abstract int getVersion();

	public abstract List<Message> getMessages();

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
					Message m = Message.readMessage(header, sb);
					messages.add(m);
					if (m instanceof ObjectHeaderContinuationMessage) {
						ObjectHeaderContinuationMessage ohcm = (ObjectHeaderContinuationMessage) m;
						header = ByteBuffer.allocate((int) ohcm.getLentgh());
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
}
