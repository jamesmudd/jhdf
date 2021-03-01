/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.storage.HdfBackingStorage;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

import static io.jhdf.Utils.stripLeadingIndex;

/**
 * Class for reading opaque data type (HDF5 type 5). Data will returned as a byte[] for each element.
 *
 * @author James Mudd
 */
class OpaqueDataType extends DataType {
    private final String asciiTag;

    public OpaqueDataType(ByteBuffer bb) {
        super(bb);

        int asciiTagLength = Utils.bitsToInt(classBits, 0, 8);

        asciiTag = Utils.readUntilNull(Utils.createSubBuffer(bb, asciiTagLength));
    }

    public String getAsciiTag() {
        return asciiTag;
    }

    @Override
    public Class<?> getJavaType() {
        return byte[].class;
    }

    @Override
    public Object fillData(ByteBuffer buffer, int[] dimensions, HdfBackingStorage hdfBackingStorage) {
        int elements = IntStream.of(dimensions)
                .reduce(1, Math::multiplyExact);
        int elementSize = buffer.remaining() / elements;

        final Object data = Array.newInstance(getJavaType(), dimensions);
        fillOpaquedData(data, dimensions, buffer, elementSize);
        return data;
    }

    private static void fillOpaquedData(Object data, int[] dims, ByteBuffer buffer, int elementSize) {
        if (dims.length > 1) {
            for (int i = 0; i < dims[0]; i++) {
                Object newArray = Array.get(data, i);
                fillOpaquedData(newArray, stripLeadingIndex(dims), buffer, elementSize);
            }
        } else {
            for (int i = 0; i < Array.getLength(data); i++) {
                byte[] bytes = new byte[elementSize];
                buffer.get(bytes);
                Array.set(data, i, bytes);
            }
        }
    }

}
