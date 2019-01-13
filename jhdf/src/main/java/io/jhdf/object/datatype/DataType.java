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
	protected final byte[] classBytes;

	public static DataType readDataType(ByteBuffer bb) {
		// Mark buffer position
		bb.mark();

		// Class and version
		BitSet classAndVersion = BitSet.valueOf(new byte[] { bb.get() });
		int version = Utils.bitsToInt(classAndVersion, 4, 4);
		int dataClass = Utils.bitsToInt(classAndVersion, 0, 4);

		if (version == 0) {
			throw new HdfException("Unreconized datatype version 0 detected");
		} else if (version == 3) {
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
			return new VariableLentgh(bb);
		default:
			throw new HdfException("Unreconised data class = " + dataClass);
		}

	}

	protected DataType(ByteBuffer bb) {

		// Class and version
		byte classAndVersion = bb.get();
		version = classAndVersion >>> 4;
		dataClass = classAndVersion & 0xF;

		classBytes = new byte[3];
		bb.get(classBytes);

		// Size
		size = bb.getInt();
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
