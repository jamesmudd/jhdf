/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.jhdf.Constants.NULL;
import static io.jhdf.Constants.SPACE;

/**
 * Data type representing strings.
 *
 * @author James Mudd
 */
public class StringData extends DataType {

	private final PaddingType paddingType;

	private final Charset charset;

	public enum PaddingType {
		NULL_TERMINATED(new NullTerminated()),
		NULL_PADDED(new NullPadded()),
		SPACE_PADDED(new SpacePadded());

		private final StringPaddingHandler stringPaddingHandler;

		PaddingType(StringPaddingHandler stringPaddingHandler) {
			this.stringPaddingHandler = stringPaddingHandler;
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
			charset = StandardCharsets.US_ASCII;
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

	/*package */ static class NullTerminated implements StringPaddingHandler {
		@Override
		public void setBufferLimit(ByteBuffer byteBuffer) {
			int i = 0;
			while (byteBuffer.get(i) != NULL) {
				i++;
			}
			// Set the limit to terminate before the null
			byteBuffer.limit(i);
		}
	}

	/*package */ static class NullPadded implements StringPaddingHandler {
		@Override
		public void setBufferLimit(ByteBuffer byteBuffer) {
			int i = byteBuffer.limit() - 1;
			while (byteBuffer.get(i) == NULL) {
				i--;
			}
			// Set the limit to terminate before the nulls
			byteBuffer.limit(i + 1);
		}
	}

	/*package */ static class SpacePadded implements StringPaddingHandler {
		@Override
		public void setBufferLimit(ByteBuffer byteBuffer) {
			int i = byteBuffer.limit() - 1;
			while (byteBuffer.get(i) == SPACE) {
				i--;
			}
			// Set the limit to terminate before the spaces
			byteBuffer.limit(i + 1);
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
