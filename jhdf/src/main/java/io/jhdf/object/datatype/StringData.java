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

/**
 * Data type representing strings.
 *
 * @author James Mudd
 */
public class StringData extends DataType {

	private final PaddingType paddingType;

	private final Charset charset;

	public enum PaddingType {
		NULL_TERMINATED,
		NULL_PADDED,
		SPACE_PADDED
	}

	public StringData(ByteBuffer bb) {
		super(bb);

		int paddingTypeValue = Utils.bitsToInt(classBits, 0, 4);
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
				throw new HdfException("Unreconized padding type. Value is: " + paddingTypeValue);
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

	public Charset getCharset() {
		return charset;
	}

	@Override
	public Class<?> getJavaType() {
		return String.class;
	}

	@Override
	public String toString() {
		return "StringData{" +
				"paddingType=" + paddingType +
				", charset=" + charset +
				'}';
	}
}
