/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.BufferBuilder;
import io.jhdf.Constants;
import io.jhdf.ObjectHeader;
import io.jhdf.Utils;
import io.jhdf.api.Attribute;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.storage.HdfBackingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class AttributeMessage extends Message {
	public static final int MESSAGE_TYPE = 12;

	private static final Logger logger = LoggerFactory.getLogger(AttributeMessage.class);

	private static final int DATA_TYPE_SHARED = 0;
	private static final int DATA_SPACE_SHARED = 1;

	private final byte version;
	private final String name;
	private final DataType dataType;
	private final DataSpace dataSpace;
	private final ByteBuffer data;

	public AttributeMessage(ByteBuffer bb, HdfBackingStorage hdfBackingStorage, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();
		logger.trace("Version: {}", version);

		final BitSet flags;
		if (version == 1) {
			// Skip reserved byte
			bb.position(bb.position() + 1);
			flags = BitSet.valueOf(new byte[0]); // No flags in v1
		} else {
			flags = BitSet.valueOf(new byte[]{bb.get()});
		}

		final int nameSize = Utils.readBytesAsUnsignedInt(bb, 2);
		final int dataTypeSize = Utils.readBytesAsUnsignedInt(bb, 2);
		final int dataSpaceSize = Utils.readBytesAsUnsignedInt(bb, 2);

		// Read name
		switch (version) {
			case 1:
				this.name = Utils.readUntilNull(Utils.createSubBuffer(bb, nameSize));
				Utils.seekBufferToNextMultipleOfEight(bb);
				break;
			case 2:
				this.name = Utils.readUntilNull(Utils.createSubBuffer(bb, nameSize));
				break;
			case 3:
				final byte characterEncoding = bb.get();
				final Charset charset;
				switch (characterEncoding) {
					case 0:
						charset = StandardCharsets.US_ASCII;
						break;
					case 1:
						charset = StandardCharsets.UTF_8;
						break;
					default:
						throw new UnsupportedHdfException("Unrecognized character set detected: " + characterEncoding);
				}
				ByteBuffer nameBuffer = Utils.createSubBuffer(bb, nameSize);
				name = charset.decode(nameBuffer).toString().trim();
				break;
			default:
				throw new UnsupportedHdfException("Unsupported Attribute message version. Detected version: " + version);
		}

		// Read data type and space
		switch (version) {
			case 1:
				dataType = DataType.readDataType(Utils.createSubBuffer(bb, dataTypeSize));
				Utils.seekBufferToNextMultipleOfEight(bb);

				dataSpace = DataSpace.readDataSpace(Utils.createSubBuffer(bb, dataSpaceSize), hdfBackingStorage.getSuperblock());
				Utils.seekBufferToNextMultipleOfEight(bb);
				break;
			case 2:
			case 3:
				if (flags.get(DATA_TYPE_SHARED)) {
					final ByteBuffer sharedMessageBuffer = Utils.createSubBuffer(bb, dataTypeSize);
					dataType = readSharedMessage(sharedMessageBuffer, hdfBackingStorage, DataTypeMessage.class).getDataType();
				} else {
					dataType = DataType.readDataType(Utils.createSubBuffer(bb, dataTypeSize));
				}

				if (flags.get(DATA_SPACE_SHARED)) {
					final ByteBuffer sharedMessageBuffer = Utils.createSubBuffer(bb, dataSpaceSize);
					dataSpace = readSharedMessage(sharedMessageBuffer, hdfBackingStorage, DataSpaceMessage.class).getDataSpace();
				} else {
					dataSpace = DataSpace.readDataSpace(Utils.createSubBuffer(bb, dataSpaceSize), hdfBackingStorage.getSuperblock());
				}
				break;
			default:
				throw new UnsupportedHdfException("Unsupported Attribute message version. Detected version: " + version);
		}

		final int dataSize = Math.toIntExact(dataSpace.getTotalLength() * dataType.getSize());
		if (dataSize == 0) {
			data = null;
		} else {
			data = Utils.createSubBuffer(bb, dataSize); // Create a new buffer starting at the current pos
		}

		logger.debug("Read attribute: Name=[{}] Datatype=[{}], Dataspace[{}]", name, dataType, dataSpace);
	}

	private <T extends Message> T readSharedMessage(ByteBuffer sharedMessageBuffer, HdfBackingStorage hdfBackingStorage, Class<T> messageType) {
		final SharedMessage sharedMessage = new SharedMessage(sharedMessageBuffer, hdfBackingStorage.getSuperblock());
		final ObjectHeader objectHeader = ObjectHeader.readObjectHeader(hdfBackingStorage, sharedMessage.getObjectHeaderAddress());
		return objectHeader.getMessageOfType(messageType);
	}

	public int getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public DataSpace getDataSpace() {
		return dataSpace;
	}

	public ByteBuffer getDataBuffer() {
		if (data == null) {
			return null;
		} else {
			// Slice the buffer to allow multiple accesses
			return data.slice().order(data.order());
		}
	}

	@Override
	public String toString() {
		return "AttributeMessage [name=" + name + ", dataType=" + dataType + ", dataSpace=" + dataSpace + "]";
	}

	@Override
	public int getMessageType() {
		return MESSAGE_TYPE;
	}

	public static AttributeMessage create(String name, Attribute attribute) {
		return new AttributeMessage(name, attribute.getDataSpace(), attribute.getDataType(), attribute.getData());
	}

	private AttributeMessage(String name, DataSpace dataSpace, DataType dataType, Object data) {
		this.name = name;
		this.version = 3;
		this.dataSpace = dataSpace;
		this.dataType = dataType;
		this.data = dataType.encodeData(data);
	}

	@Override
	public ByteBuffer toBuffer() {

		byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		ByteBuffer dataTypeBytes = dataType.toBuffer();
		ByteBuffer dataSpaceBytes = dataSpace.toBuffer();

		return new BufferBuilder()
			.writeByte(3) // version
			.writeByte(0) // flags
			.writeShort(nameBytes.length + 1) // +1 for null terminated
			.writeShort(dataTypeBytes.capacity())
			.writeShort(dataSpaceBytes.capacity())
			.writeByte(1) // name charset UTF8
			.writeBytes(nameBytes)
			.writeByte(Constants.NULL) // Null terminated string
			.writeBuffer(dataTypeBytes)
			.writeBuffer(dataSpaceBytes)
			.writeBuffer(data)
			.build();
	}
}
