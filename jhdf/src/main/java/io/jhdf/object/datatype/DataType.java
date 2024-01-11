/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

public abstract class DataType {

	private final int version;
	private final int dataClass;
	private final int size; // In bytes
	protected final BitSet classBits;

	public DataType(int dataClass, int size) {
		this.version = 0;
		this.dataClass = dataClass;
		this.size = size;
		this.classBits = new BitSet(8); // TODO check size
	}

	public static DataType readDataType(ByteBuffer bb) {
		// Mark buffer position
		bb.mark();

		// Class and version
		final BitSet classAndVersion = BitSet.valueOf(new byte[]{bb.get()});
		int version = Utils.bitsToInt(classAndVersion, 4, 4);
		int dataClass = Utils.bitsToInt(classAndVersion, 0, 4);

		if (version == 0 || version > 3) {
			throw new HdfException("Unrecognized datatype version '" + version + "' detected");
		}

		// Move the buffer back to the start of the data type message
		bb.reset();

		switch (dataClass) {
			case 0: // Fixed point
				return new FixedPoint(bb);
			case 1: // Floating point
				return new FloatingPoint(bb);
			case 2: // Time
				throw new UnsupportedHdfException("Time data type is not yet supported");
			case 3: // String
				return new StringData(bb);
			case 4: // Bit field
				return new BitField(bb);
			case 5: // Opaque
				return new OpaqueDataType(bb);
			case 6: // Compound
				return new CompoundDataType(bb);
			case 7: // Reference
				return new Reference(bb);
			case 8: // Enum
				return new EnumDataType(bb);
			case 9: // Variable length
				return new VariableLength(bb);
			case 10: // Array
				return new ArrayDataType(bb);
			default:
				throw new HdfException("Unrecognized data class = " + dataClass);
		}

	}

	protected DataType(ByteBuffer bb) {

		// Class and version
		final BitSet classAndVersion = BitSet.valueOf(new byte[]{bb.get()});
		dataClass = Utils.bitsToInt(classAndVersion, 0, 4);
		version = Utils.bitsToInt(classAndVersion, 4, 4);

		byte[] classBytes = new byte[3];
		bb.get(classBytes);
		classBits = BitSet.valueOf(classBytes);

		// Size
		size = Utils.readBytesAsUnsignedInt(bb, 4);
	}

	public static DataType fromObject(Object data) {
		if (data.getClass().isArray()) {
			Class<?> type = Utils.getArrayType(data);
			if (type.equals(int.class)) {
				return new FixedPoint((short) 4);
			} else if (type.equals(double.class)) {
				return new FloatingPoint(8);
			}
			throw new HdfException("Error");

		} else {
			throw new UnsupportedHdfException("Only arrays can be written at the moment");
		}
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

	/**
	 * Get the Java {@link Class} of this HDF5 type
	 *
	 * @return {@link Class} of this HDF5 type
	 */
	public abstract Class<?> getJavaType();

	/**
	 * Fill the data from buffer into the returned {@link Object} using this {@link DataType}
	 *
	 * @param buffer            containing the data
	 * @param dimensions        dataset dimensions
	 * @param hdfBackingStorage file containing this data
	 * @return the read data
	 */
	public abstract Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage);

}
