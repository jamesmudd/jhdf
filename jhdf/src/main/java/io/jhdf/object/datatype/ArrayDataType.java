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

import io.jhdf.dataset.DatasetReader;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import static io.jhdf.Utils.readBytesAsUnsignedInt;

/**
 * Class for reading array data type messages.
 */
public class ArrayDataType extends DataType {

	private final DataType baseType;
	private final int[] arrayTypeDimensions;

	public ArrayDataType(ByteBuffer bb) {
		super(bb);

		final int dimensionsSize = readBytesAsUnsignedInt(bb, 1);
		arrayTypeDimensions = new int[dimensionsSize];

		if (getVersion() == 2) {
			// Skip 3 bytes
			bb.position(bb.position() + 3);
		}

		for (int i = 0; i < arrayTypeDimensions.length; i++) {
			arrayTypeDimensions[i] = readBytesAsUnsignedInt(bb, 4);

			if (getVersion() == 2) {
				// Skip Permutation Index not supported anyway
				bb.position(bb.position() + 4);
			}
		}

		baseType = DataType.readDataType(bb);
	}

	@Override
	public Class<?> getJavaType() {
		return Array.newInstance(baseType.getJavaType(), 0).getClass();
	}

	public DataType getBaseType() {
		return baseType;
	}

	/**
	 * Get the instance dimensions
	 * @return instance dimensions
	 */
	public int[] getArrayTypeDimensions() {
		return ArrayUtils.clone(arrayTypeDimensions);
	}

	@Override
	public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
		final int[] mergedDimensions = ArrayUtils.addAll(dimensions, arrayTypeDimensions);
		return DatasetReader.readDataset(getBaseType(), buffer, mergedDimensions, hdfBackingStorage);
	}
}
