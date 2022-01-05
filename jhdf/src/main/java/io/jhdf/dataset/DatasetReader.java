/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.object.datatype.DataType;
import io.jhdf.storage.HdfBackingStorage;

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

	private DatasetReader() {
		throw new AssertionError("No instances of DatasetReader");
	}

	/**
	 * This converts a buffer into a Java object representing this dataset.
	 *
	 * @param type              The data type of this dataset
	 * @param buffer            The buffer containing the dataset
	 * @param dimensions        The dimensions of this dataset
	 * @param hdfBackingStorage The file channel for reading the file
	 * @return A Java object representation of this dataset
	 */
	public static Object readDataset(DataType type, ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		// If the data is scalar make a fake one element array then remove it at the end

		final boolean isScalar;
		if (dimensions.length == 0) {
			// Scalar dataset
			isScalar = true;
			dimensions = new int[]{1}; // Fake the dimensions
		} else {
			isScalar = false;
		}

		final Object data = type.fillData(buffer, dimensions, hdfBackingStorage);

		if (isScalar) {
			return Array.get(data, 0);
		} else {
			return data;
		}
	}
}
