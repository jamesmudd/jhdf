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

public class StringData extends DataType {

	private final boolean nullTerminated;
	private final boolean nullPad;
	private final boolean spacePad;

	private final Charset charset;

	public StringData(ByteBuffer bb) {
		super(bb);

		nullTerminated = classBits.get(0);
		nullPad = classBits.get(1);
		spacePad = classBits.get(2);

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

	public boolean isNullTerminated() {
		return nullTerminated;
	}

	public boolean isNullPad() {
		return nullPad;
	}

	public boolean isSpacePad() {
		return spacePad;
	}

	public Charset getCharset() {
		return charset;
	}

	@Override
	public String toString() {
		return "StringData [nullTerminated=" + nullTerminated + ", nullPad=" + nullPad + ", spacePad=" + spacePad
				+ ", charset=" + charset + "]";
	}

	@Override
	public Class<?> getJavaType() {
		return String.class;
	}

}
