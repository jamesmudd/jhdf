/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.object.datatype.ArrayDataType;
import io.jhdf.object.datatype.BitField;
import io.jhdf.object.datatype.CompoundDataType;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.EnumDataType;
import io.jhdf.object.datatype.FixedPoint;
import io.jhdf.object.datatype.FloatingPoint;
import io.jhdf.object.datatype.Reference;
import io.jhdf.object.datatype.StringData;
import io.jhdf.object.datatype.VariableLength;

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

import static io.jhdf.Utils.stripLeadingIndex;

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

	/**
	 * This converts a buffer into a Java object representing this dataset.
	 *
	 * @param type The data type of this dataset
	 * @param buffer The buffer containing the dataset
	 * @param size The number of elements in this dataset
	 * @param dimensions The dimensions of this dataset
	 * @param hdfFc The file channel of the file containing this dataset
	 * @return A Java object representation of this dataset
	 */
	public static Object readDataset(DataType type, ByteBuffer buffer, long size, int[] dimensions, HdfFileChannel hdfFc) {
		// Make the array to hold the data
		Class<?> javaType = type.getJavaType();

		// If the data is scalar make a fake one element array then remove it at the end
		Object data;
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
			data = fixedPoint.fillData(dimensions, buffer);
		} else if (type instanceof FloatingPoint) {
			FloatingPoint floatingPoint = (FloatingPoint) type;
			data = floatingPoint.fillData(dimensions, buffer);

		} else if (type instanceof StringData) {
			final int stringLength = type.getSize();
			final Charset charset = ((StringData) type).getCharset();
			final StringData.StringPaddingHandler stringPaddingHandler = ((StringData) type).getStringPaddingHandler();
			fillFixedLengthStringData(data, dimensions, buffer, stringLength, charset, stringPaddingHandler);
		} else if (type instanceof BitField) {
			final BitField bitField = (BitField) type;
			fillBitfieldData(data, dimensions, buffer.order(bitField.getByteOrder()));
//		} else if (type instanceof Reference) {
//			//reference type handles addresses, which are always longs for this library
//			int elementSize = type.getSize();
//			if (elementSize == 8) {
//				fillData(data, dimensions, buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer());
//			} else if (elementSize < 8) {
//				fillLongData(data, dimensions, buffer.order(ByteOrder.LITTLE_ENDIAN), elementSize);
//			} else {
//				throw new HdfTypeException("Unsupported address size in reference data type " + elementSize + " bytes");
//			}
		} else if (type instanceof ArrayDataType) {
			final ArrayDataType arrayType = (ArrayDataType) type;
			if (dimensions.length !=1) {
				throw new HdfException("Multi dimension array data types are not supported");
			}

			for (int i = 0; i < dimensions[0]; i++) {
				// Need to position the buffer ready for the read
				buffer.position(i * arrayType.getBaseType().getSize() * arrayType.getDimensions()[0]);
				Object elementDataset = readDataset(arrayType.getBaseType(), buffer, size, arrayType.getDimensions(), hdfFc);
				Array.set(data, i, elementDataset);
			}
		} else if (type instanceof EnumDataType) {
			return EnumDatasetReader.readEnumDataset((EnumDataType) type, buffer, dimensions);
		} else if (type instanceof VariableLength) {
			data = VariableLengthDatasetReader.readDataset((VariableLength) type, buffer, size, dimensions, hdfFc);
		} else if (type instanceof CompoundDataType) {
			data = CompoundDatasetReader.readDataset((CompoundDataType) type, buffer, size, dimensions, hdfFc);
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

	private static void fillBitfieldData(Object data, int[] dims, ByteBuffer buffer) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillBitfieldData(newArray, stripLeadingIndex(dims), buffer);
			}
		} else {
			for (int i = 0; i < Array.getLength(data); i++) {
				Array.set(data, i, buffer.get() == 1);
			}
		}
	}

	// String Data

	private static void fillFixedLengthStringData(Object data, int[] dims, ByteBuffer buffer, int stringLength, Charset charset, StringData.StringPaddingHandler stringPaddingHandler) {
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

}
