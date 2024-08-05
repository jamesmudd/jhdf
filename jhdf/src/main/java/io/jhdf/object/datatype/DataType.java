/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.BufferBuilder;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;

import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class DataType {

	private final int version;
	private final int dataClass;
	private final int size; // In bytes
	protected final BitSet classBits;

	protected DataType(int dataClass, int size) {
		this.version = 1;
		this.dataClass = dataClass;
		this.size = size;
		this.classBits = new BitSet(24);
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

		// TODO all class IDs
		switch (dataClass) {
			case FixedPoint.CLASS_ID: // Fixed point
				return new FixedPoint(bb);
			case FloatingPoint.CLASS_ID: // Floating point
				return new FloatingPoint(bb);
			case 2: // Time
				throw new UnsupportedHdfException("Time data type is not yet supported");
			case StringData.CLASS_ID: // String
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
		final Class<?> type = Utils.getType(data);

		if (type == byte.class || type == Byte.class) {
			return new FixedPoint(1);
		} else if (type == short.class || type == Short.class) {
			return new FixedPoint(2);
		} else if (type == int.class || type == Integer.class) {
			return new FixedPoint(4);
		} else if (type == long.class || type == Long.class) {
			return new FixedPoint(8);
		} else if (type == float.class || type == Float.class) {
			return FloatingPoint.FLOAT;
		} else if (type == double.class || type == Double.class) {
			return FloatingPoint.DOUBLE;
		} else if (type == String.class) {
			return StringData.create(data); // TODO
		} else {
			throw new HdfException("Could not create DataType for: " + type);
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

	// TODO could be abstract when there are more impls
	public ByteBuffer encodeData(Object data){
		throw new UnsupportedHdfException("Data type [" + getClass().getSimpleName() + "] does not support writing");
	}

	// TODO could be abstract when there are more impls
	public ByteBuffer toBuffer() {
		throw new UnsupportedHdfException("Data type [" + getClass().getSimpleName() + "] does not support writing");
	}

	protected BufferBuilder toBufferBuilder() {
		BitSet classAndVersion = new BitSet(8);
		Utils.writeIntToBits(dataClass, classAndVersion, 0, 4);
		Utils.writeIntToBits(version, classAndVersion, 4, 4);

		return new BufferBuilder()
			.writeBitSet(classAndVersion,1)
			.writeBitSet(classBits, 3)
			.writeInt(getSize());
	}

	public void writeData(Object data, int[] dimensions, HdfFileChannel hdfFileChannel) {
		throw new UnsupportedHdfException("Data type [" + getClass().getSimpleName() + "] does not support writing");
	}
}
