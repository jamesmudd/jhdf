/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import static io.jhdf.Utils.readBytesAsUnsignedInt;

/**
 * Class for reading array data type messages.
 */
public class ArrayDataType extends DataType {

    private final DataType baseType;
    private final int[] dimensions;


    public ArrayDataType(ByteBuffer bb) {
        super(bb);

        final int dimensionsSize = readBytesAsUnsignedInt(bb, 1);
        dimensions = new int[dimensionsSize];

        if(getVersion() == 2) {
            // Skip 3 bytes
            bb.position(bb.position() + 3);
        }

        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = readBytesAsUnsignedInt(bb, 4);

            if(getVersion() == 2) {
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

    public int[] getDimensions() {
        return dimensions;
    }
}
