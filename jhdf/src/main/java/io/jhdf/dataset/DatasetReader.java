/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.object.datatype.ArrayDataType;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.EnumDataType;
import io.jhdf.object.datatype.FixedPoint;
import io.jhdf.object.datatype.FloatingPoint;
import io.jhdf.object.datatype.Reference;
import io.jhdf.object.datatype.StringData;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * <p>
 * This class handles converting the {@link ByteBuffer} obtained from the file
 * into a Java array containing the data. It makes use of Java NIO ByteBuffers
 * bulk read methods where possible to enable high performance IO.
 * </p>
 * Some useful information about HDF5 → Java type mappings see:
 * <ul>
 * <li><a href=
 * "https://support.hdfgroup.org/ftp/HDF5/prev-releases/HDF-JAVA/hdfjni-3.2.1/hdf5_java_doc/hdf/hdf5lib/H5.html">HDF5
 * Java wrapper H5.java</a></li>
 * <li><a href="http://docs.h5py.org/en/stable/faq.html">h5py FAQ</a></li>
 * <li><a href=
 * "https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">Java
 * primitive types</a></li>
 * </ul>
 *
 * @author James Mudd
 */
public final class DatasetReader {

	/** No instances */
	private DatasetReader() {
		throw new AssertionError("No instances of DatasetReader");
	}

	public static Object readDataset(DataType type, ByteBuffer buffer, int[] dimensions) {
		// Make the array to hold the data
		Class<?> javaType = type.getJavaType();

		// If the data is scalar make a fake one element array then remove it at the end
		final Object data;
		final boolean isScalar;
		if (dimensions.length == 0) {
			// Scalar dataset
			data = Array.newInstance(javaType, 1);
			isScalar = true;
			dimensions = new int[]{1}; // Fake the dimensions
		} else {
			data = Array.newInstance(javaType, dimensions);
			isScalar = false;
		}

		if (type instanceof FixedPoint) {
			FixedPoint fixedPoint = (FixedPoint) type;
			ByteOrder byteOrder = fixedPoint.getByteOrder();
			if (fixedPoint.isSigned()) {
				switch (fixedPoint.getSize()) {
					case 1:
						fillData(data, dimensions, buffer.order(byteOrder));
						break;
					case 2:
						fillData(data, dimensions, buffer.order(byteOrder).asShortBuffer());
						break;
					case 4:
						fillData(data, dimensions, buffer.order(byteOrder).asIntBuffer());
						break;
					case 8:
						fillData(data, dimensions, buffer.order(byteOrder).asLongBuffer());
						break;
					default:
						throw new HdfTypeException(
								"Unsupported signed integer type size " + fixedPoint.getSize() + " bytes");
				}
			} else { // Unsigned
				switch (fixedPoint.getSize()) {
					case 1:
						fillDataUnsigned(data, dimensions, buffer.order(byteOrder));
						break;
					case 2:
						fillDataUnsigned(data, dimensions, buffer.order(byteOrder).asShortBuffer());
						break;
					case 4:
						fillDataUnsigned(data, dimensions, buffer.order(byteOrder).asIntBuffer());
						break;
					case 8:
						fillDataUnsigned(data, dimensions, buffer.order(byteOrder).asLongBuffer());
						break;
					default:
						throw new HdfTypeException(
								"Unsupported signed integer type size " + fixedPoint.getSize() + " bytes");
				}
			}
		} else if (type instanceof FloatingPoint) {
			FloatingPoint floatingPoint = (FloatingPoint) type;
			ByteOrder byteOrder = floatingPoint.getByteOrder();

			switch (floatingPoint.getSize()) {
				case 4:
					fillData(data, dimensions, buffer.order(byteOrder).asFloatBuffer());
					break;
				case 8:
					fillData(data, dimensions, buffer.order(byteOrder).asDoubleBuffer());
					break;
				default:
					throw new HdfTypeException(
							"Unsupported floating point type size " + floatingPoint.getSize() + " bytes");
			}
		} else if (type instanceof StringData) {
			final int stringLength = type.getSize();
			final Charset charset = ((StringData) type).getCharset();
			fillFixedLengthStringData(data, dimensions, buffer, stringLength, charset);
		} else if (type instanceof Reference) {
			//reference type handles addresses, which are always longs for this library
			int size = type.getSize();
			if (size == 8) {
				fillData(data, dimensions, buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer());
			} else if (size < 8) {
				fillLongData(data, dimensions, buffer.order(ByteOrder.LITTLE_ENDIAN), size);
			} else {
				throw new HdfTypeException("Unsupported address size in reference data type " + size + " bytes");
			}
		} else if (type instanceof ArrayDataType) {
			final ArrayDataType arrayType = (ArrayDataType) type;
			if (dimensions.length !=1) {
				throw new HdfException("Multi dimension array data types are not supported");
			}

			for (int i = 0; i < dimensions[0]; i++) {
				// Need to position the buffer ready for the read
				buffer.position(i * arrayType.getBaseType().getSize() * arrayType.getDimensions()[0]);
				Object elementDataset = readDataset(arrayType.getBaseType(), buffer, arrayType.getDimensions());
				Array.set(data, i, elementDataset);
			}
		} else if (type instanceof EnumDataType) {
			final EnumDataType enumDataType = (EnumDataType) type;
			return EnumDatasetReader.readEnumDataset(enumDataType, buffer, dimensions);
		} else {
			throw new HdfException(
					"DatasetReader was passed a type it cant fill. Type: " + type.getClass().getCanonicalName());
		}

		if (isScalar) {
			return Array.get(data, 0);
		} else {
			return data;
		}
	}

	// Signed Fixed Point

	private static void fillData(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((byte[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, ShortBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((short[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, IntBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((int[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, LongBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((long[]) data);
		}
	}

	private static void fillLongData(Object data, int[] dims, ByteBuffer buffer, int size) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillLongData(newArray, stripLeadingIndex(dims), buffer, size);
			}
		} else {
			long[] longData = (long[]) data;
			for (int i = 0; i < longData.length; i++) {
				longData[i] = Utils.readBytesAsUnsignedLong(buffer, size);
			}
		}
	}

	// Unsigned Fixed Point

	private static void fillDataUnsigned(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final byte[] tempBuffer = new byte[dims[dims.length - 1]];
			buffer.get(tempBuffer);
			// Convert to unsigned
			int[] intData = (int[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				intData[i] = Byte.toUnsignedInt(tempBuffer[i]);
			}
		}
	}

	private static void fillDataUnsigned(Object data, int[] dims, ShortBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final short[] tempBuffer = new short[dims[dims.length - 1]];
			buffer.get(tempBuffer);
			// Convert to unsigned
			int[] intData = (int[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				intData[i] = Short.toUnsignedInt(tempBuffer[i]);
			}
		}
	}

	private static void fillDataUnsigned(Object data, int[] dims, IntBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final int[] tempBuffer = new int[dims[dims.length - 1]];
			buffer.get(tempBuffer);
			// Convert to unsigned
			long[] longData = (long[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				longData[i] = Integer.toUnsignedLong(tempBuffer[i]);
			}
		}
	}

	private static void fillDataUnsigned(Object data, int[] dims, LongBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			final long[] tempBuffer = new long[dims[dims.length - 1]];
			final ByteBuffer tempByteBuffer = ByteBuffer.allocate(8);
			buffer.get(tempBuffer);
			BigInteger[] bigIntData = (BigInteger[]) data;
			for (int i = 0; i < tempBuffer.length; i++) {
				tempByteBuffer.putLong(0, tempBuffer[i]);
				bigIntData[i] = new BigInteger(1, tempByteBuffer.array());
			}
		}
	}

	// Floating Point

	private static void fillData(Object data, int[] dims, FloatBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((float[]) data);
		}
	}

	private static void fillData(Object data, int[] dims, DoubleBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			buffer.get((double[]) data);
		}
	}

	// String Data

	private static void fillFixedLengthStringData(Object data, int[] dims, ByteBuffer buffer, int stringLength, Charset charset) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillFixedLengthStringData(newArray, stripLeadingIndex(dims), buffer, stringLength, charset);
			}
		} else {
			for (int i = 0; i < dims[0]; i++) {
				buffer.position(i * stringLength);
				ByteBuffer elementBuffer = Utils.createSubBuffer(buffer, stringLength);
				Array.set(data, i, charset.decode(elementBuffer).toString().trim());
			}
		}
	}

	// Utils

	private static int[] stripLeadingIndex(int[] dims) {
		return Arrays.copyOfRange(dims, 1, dims.length);
	}

}
