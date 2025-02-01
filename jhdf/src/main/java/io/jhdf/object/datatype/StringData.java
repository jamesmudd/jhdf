/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.BufferBuilder;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static io.jhdf.Constants.NULL;
import static io.jhdf.Constants.SPACE;
import static io.jhdf.Utils.stripLeadingIndex;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Data type representing strings.
 *
 * @author James Mudd
 */
public class StringData extends DataType {

	public static final int CLASS_ID = 3;
	private final PaddingType paddingType;

	private final Charset charset;

	@Override
	public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final Object data = Array.newInstance(getJavaType(), dimensions);
		fillFixedLengthStringData(data, dimensions, buffer, getSize(), getCharset(), getStringPaddingHandler());
		return data;
	}

	private static void fillFixedLengthStringData(Object data, int[] dims, ByteBuffer buffer, int stringLength, Charset charset, StringPaddingHandler stringPaddingHandler) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillFixedLengthStringData(newArray, stripLeadingIndex(dims), buffer, stringLength, charset, stringPaddingHandler);
			}
		} else {
			for (int i = 0; i < dims[0]; i++) {
				ByteBuffer elementBuffer = Utils.createSubBuffer(buffer, stringLength);
				stringPaddingHandler.setBufferLimit(elementBuffer);
				Array.set(data, i, charset.decode(elementBuffer).toString());
			}
		}
	}

	public enum PaddingType {
		NULL_TERMINATED(new NullTerminated(), 0),
		NULL_PADDED(new NullPadded(), 1),
		SPACE_PADDED(new SpacePadded(), 2);

		private final StringPaddingHandler stringPaddingHandler;
		private final int id;

		PaddingType(StringPaddingHandler stringPaddingHandler, int id) {
			this.stringPaddingHandler = stringPaddingHandler;
			this.id = id;
		}
	}

	public StringData(ByteBuffer bb) {
		super(bb);

		final int paddingTypeValue = Utils.bitsToInt(classBits, 0, 4);
		switch (paddingTypeValue) {
			case 0:
				paddingType = PaddingType.NULL_TERMINATED;
				break;
			case 1:
				paddingType = PaddingType.NULL_PADDED;
				break;
			case 2:
				paddingType = PaddingType.SPACE_PADDED;
				break;
			default:
				throw new HdfException("Unrecognized padding type. Value is: " + paddingTypeValue);
		}

		final int charsetIndex = Utils.bitsToInt(classBits, 4, 4);
		switch (charsetIndex) {
			case 0:
				charset = US_ASCII;
				break;
			case 1:
				charset = StandardCharsets.UTF_8;
				break;
			default:
				throw new HdfException("Unrecognized Charset. Index is: " + charsetIndex);
		}
	}

	public PaddingType getPaddingType() {
		return paddingType;
	}

	public StringPaddingHandler getStringPaddingHandler() {
		return paddingType.stringPaddingHandler;
	}

	public Charset getCharset() {
		return charset;
	}

	@Override
	public Class<?> getJavaType() {
		return String.class;
	}

	public interface StringPaddingHandler {
		void setBufferLimit(ByteBuffer byteBuffer);
	}

	/* package */ static class NullTerminated implements StringPaddingHandler {
		@Override
		public void setBufferLimit(ByteBuffer byteBuffer) {
			final int limit = byteBuffer.limit();
			int i = 0;
			while (i < limit && byteBuffer.get(i) != NULL) {
				i++;
			}
			// Set the limit to terminate before the null
			byteBuffer.limit(i);
		}
	}

	/* package */ static class NullPadded implements StringPaddingHandler {
		@Override
		public void setBufferLimit(ByteBuffer byteBuffer) {
			int i = byteBuffer.limit() - 1;
			while (i >= 0 && byteBuffer.get(i) == NULL) {
				i--;
			}
			// Set the limit to terminate before the nulls
			byteBuffer.limit(i + 1);
		}
	}

	/* package */ static class SpacePadded implements StringPaddingHandler {
		@Override
		public void setBufferLimit(ByteBuffer byteBuffer) {
			int i = byteBuffer.limit() - 1;
			while (i >= 0 && byteBuffer.get(i) == SPACE) {
				i--;
			}
			// Set the limit to terminate before the spaces
			byteBuffer.limit(i + 1);
		}
	}

	public static StringData create(Object data) {
		int maxLength = Arrays.stream(Utils.flatten(data))
			.map(String.class::cast)
			.mapToInt(s -> StandardCharsets.UTF_8.encode(s).limit())
			.max().getAsInt();

		return new StringData(PaddingType.NULL_TERMINATED, StandardCharsets.UTF_8, maxLength);
	}

	private StringData(PaddingType paddingType, Charset charset, int maxLength) {
		super(CLASS_ID, maxLength + 1); // +1 for padding
		this.paddingType = paddingType;
		this.charset = charset;
	}

	@Override
	public ByteBuffer toBuffer() {
		Utils.writeIntToBits(paddingType.id, classBits, 0, 4);
		Utils.writeIntToBits(1, classBits, 4, 4); // Always UTF8
		return super.toBufferBuilder().build();
	}

	@Override
	public void writeData(Object data, int[] dimensions, HdfFileChannel hdfFileChannel) {
		if (data.getClass().isArray()) {
			final int fastDimSize = dimensions[dimensions.length - 1];
			final ByteBuffer buffer = ByteBuffer.allocate(fastDimSize * getSize());
			writeArrayData(data, dimensions, buffer, hdfFileChannel);
		} else {
			writeScalarData(data, hdfFileChannel);
		}
	}

	private void writeScalarData(Object data, HdfFileChannel hdfFileChannel) {
		ByteBuffer buffer = encodeScalarData(data);
		buffer.rewind();
		hdfFileChannel.write(buffer);
	}

	private void writeArrayData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeArrayData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel);
			}
		} else {
			String[] strings = (String[]) data;
			for (int i = 0; i < strings.length; i++) {
				String str = strings[i];
				buffer.put(charset.encode(str))
					.put(NULL)
					.position((i + 1) * getSize());
			}
			buffer.rewind();
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}

	private ByteBuffer encodeScalarData(Object data) {
		return new BufferBuilder()
			.writeBuffer(charset.encode((String) data))
			.writeByte(NULL)
			.build();
	}

	@Override
	public ByteBuffer encodeData(Object data) {
		Objects.requireNonNull(data, "Cannot encode null");

		if (data.getClass().isArray()) {
			final int[] dimensions = Utils.getDimensions(data);
			final int totalElements = Arrays.stream(dimensions).reduce(1, Math::multiplyExact);
			final ByteBuffer buffer = ByteBuffer.allocate(totalElements * getSize());
			encodeDataInternal(data, dimensions, buffer);
			return buffer;
		} else {
			return encodeScalarData(data);
		}
	}

	private void encodeDataInternal(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				encodeDataInternal(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final int offset = buffer.position();
            String[] strings = (String[]) data;
            for (int i = 0; i < strings.length; i++) {
                String str = strings[i];
                buffer.put(this.charset.encode(str))
                        .put(NULL)
                        .position(offset + (i + 1) * getSize());
            }
		}
	}

	@Override
	public String toString() {
		return "StringData{" +
			"paddingType=" + paddingType +
			", charset=" + charset +
			'}';
	}
}
