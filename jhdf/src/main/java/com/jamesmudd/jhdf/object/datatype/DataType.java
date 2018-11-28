package com.jamesmudd.jhdf.object.datatype;

import java.nio.ByteBuffer;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class DataType {

	private final int version;
	private final int dataClass;
	private int size; // In bytes
	byte[] classBytes;

	public static DataType readDataType(ByteBuffer bb) {
		// Cache buffer position
		int pos = bb.position();

		// Class and version
		byte classAndVersion = bb.get();
		int version = classAndVersion >>> 4;
		int dataClass = classAndVersion & 0xF;

		// Move the buffer back to the start of the data type message
		bb.position(pos);

		switch (dataClass) {
		case 0: // Fixed point
			return new FixedPoint(bb);
		case 1:
			return new FloatingPoint(bb);
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
