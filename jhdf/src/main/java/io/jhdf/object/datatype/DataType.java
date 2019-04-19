/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.object.datatype;

import java.nio.ByteBuffer;
import java.util.BitSet;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

public abstract class DataType {

	private final int version;
	private final int dataClass;
	private final int size; // In bytes
	protected final BitSet classBits;

	public static DataType readDataType(ByteBuffer bb) {
		// Mark buffer position
		bb.mark();

		// Class and version
		BitSet classAndVersion = BitSet.valueOf(new byte[] { bb.get() });
		int version = Utils.bitsToInt(classAndVersion, 4, 4);
		int dataClass = Utils.bitsToInt(classAndVersion, 0, 4);

		if (version == 0) {
			throw new HdfException("Unreconized datatype version 0 detected");
		}
		if (version == 3) {
			throw new UnsupportedHdfException("VAX byte ordered datatype encountered");
		}

		// Move the buffer back to the start of the data type message
		bb.reset();

		switch (dataClass) {
		case 0: // Fixed point
			return new FixedPoint(bb);
		case 1:
			return new FloatingPoint(bb);
		case 3:
			return new StringData(bb);
		case 9: // Variable length
			return new VariableLength(bb);
		default:
			throw new HdfException("Unreconised data class = " + dataClass);
		}

	}

	protected DataType(ByteBuffer bb) {

		// Class and version
		BitSet classAndVersion = BitSet.valueOf(new byte[] { bb.get() });
		version = Utils.bitsToInt(classAndVersion, 4, 4);
		dataClass = Utils.bitsToInt(classAndVersion, 0, 4);

		byte[] classBytes = new byte[3];
		bb.get(classBytes);
		classBits = BitSet.valueOf(classBytes);

		// Size
		size = Utils.readBytesAsUnsignedInt(bb, 4);
	}

	public int getVersion() {
		return version;
	}

	public int getDataClass() {
		return dataClass;
	}

	/**
	 * Gets the number of bytes used to represent each element
	 *
	 * @return the size of each element in bytes
	 */
	public int getSize() {
		return size;
	}

	public abstract Class<?> getJavaType();

}
