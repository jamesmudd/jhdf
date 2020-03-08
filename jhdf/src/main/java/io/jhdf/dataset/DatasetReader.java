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
import io.jhdf.exceptions.HdfException;
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
import java.nio.ByteBuffer;

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
	 * @param dimensions The dimensions of this dataset
	 * @param hdfFc
	 * @return A Java object representation of this dataset
	 */
	public static Object readDataset(DataType type, ByteBuffer buffer, int[] dimensions, HdfFileChannel hdfFc) {
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
			final StringData stringData = (StringData) type;
			data = stringData.fillData(dimensions, buffer);
		} else if (type instanceof BitField) {
			final BitField bitField = (BitField) type;
			data = bitField.fillData(dimensions, buffer);
		} else if (type instanceof Reference) {
			Reference reference = (Reference) type;
			data = reference.fillData(dimensions, buffer);
		} else if (type instanceof ArrayDataType) {
			final ArrayDataType arrayType = (ArrayDataType) type;
			if (dimensions.length !=1) {
				throw new HdfException("Multi dimension array data types are not supported");
			}

			for (int i = 0; i < dimensions[0]; i++) {
				// Need to position the buffer ready for the read
				buffer.position(i * arrayType.getBaseType().getSize() * arrayType.getDimensions()[0]);
				Object elementDataset = readDataset(arrayType.getBaseType(), buffer, arrayType.getDimensions(), hdfFc);
				Array.set(data, i, elementDataset);
			}
		} else if (type instanceof EnumDataType) {
			EnumDataType enumDataType = (EnumDataType) type;
			data = enumDataType.fillData(dimensions, buffer, hdfFc);
		} else if (type instanceof VariableLength) {
			VariableLength variableLength = (VariableLength) type;
			data = variableLength.fillData(dimensions, buffer, hdfFc);
		} else if (type instanceof CompoundDataType) {
			CompoundDataType compoundDataType = (CompoundDataType) type;
			data = compoundDataType.fillData(buffer, dimensions, hdfFc);
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
}
