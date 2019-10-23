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

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.EnumDataType;
import io.jhdf.object.datatype.FixedPoint;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 * Special case of dataset reader for filling enum datasets, handles converting integer values into the enum strings on
 * the fly while filling. In the future might want to consider if this is actually what people want, maybe a way to read
 * the integer data directly could be useful or even the option to pass in a Java enum and get back a typed array.
 *
 * @author James Mudd
 */
public class EnumDatasetReader {

    /** No instances */
    private EnumDatasetReader() {
        throw new AssertionError("No instances of EnumDatasetReader");
    }

    public static Object readEnumDataset(EnumDataType enumDataType, ByteBuffer buffer, int[] dimensions) {

        final DataType baseType = enumDataType.getBaseType();
        if (baseType instanceof FixedPoint) {
            Object data = Array.newInstance(String.class, dimensions);

            FixedPoint fixedPoint = (FixedPoint) baseType;

            switch (fixedPoint.getSize()) {
                case 1:
                    fillDataUnsigned(data, dimensions, buffer.order(fixedPoint.getByteOrder()), enumDataType.getEnumMapping());
                    break;
                case 2:
                    fillDataUnsigned(data, dimensions, buffer.order(fixedPoint.getByteOrder()).asShortBuffer(), enumDataType.getEnumMapping());
                    break;
                case 4:
                    fillDataUnsigned(data, dimensions, buffer.order(fixedPoint.getByteOrder()).asIntBuffer(), enumDataType.getEnumMapping());
                    break;
                case 8:
                    fillDataUnsigned(data, dimensions, buffer.order(fixedPoint.getByteOrder()).asLongBuffer(), enumDataType.getEnumMapping());
                    break;
                default:
                    throw new HdfTypeException(
                            "Unsupported signed integer type size " + fixedPoint.getSize() + " bytes");
            }

            return data;
        } else {
            throw new HdfException("Trying to fill enum dataset with non-integer base type: " + baseType);
        }
    }

    private static void fillDataUnsigned(Object data, int[] dims, ByteBuffer buffer, Map<Integer, String> enumMapping) {
        if (dims.length > 1) {
            for (int i = 0; i < dims[0]; i++) {
                Object newArray = Array.get(data, i);
                fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer, enumMapping);
            }
        } else {
            final byte[] tempBuffer = new byte[dims[dims.length - 1]];
            buffer.get(tempBuffer);
            // Convert to enum values
            String[] stringData = (String[]) data;
            for (int i = 0; i < tempBuffer.length; i++) {
                stringData[i] = enumMapping.get(Byte.toUnsignedInt(tempBuffer[i]));
            }
        }
    }

    private static void fillDataUnsigned(Object data, int[] dims, ShortBuffer buffer, Map<Integer, String> enumMapping) {
        if (dims.length > 1) {
            for (int i = 0; i < dims[0]; i++) {
                Object newArray = Array.get(data, i);
                fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer, enumMapping);
            }
        } else {
            final short[] tempBuffer = new short[dims[dims.length - 1]];
            buffer.get(tempBuffer);
            // Convert to enum values
            String[] stringData = (String[]) data;
            for (int i = 0; i < tempBuffer.length; i++) {
                stringData[i] = enumMapping.get(Short.toUnsignedInt(tempBuffer[i]));
            }
        }
    }

    private static void fillDataUnsigned(Object data, int[] dims, IntBuffer buffer, Map<Integer, String> enumMapping) {
        if (dims.length > 1) {
            for (int i = 0; i < dims[0]; i++) {
                Object newArray = Array.get(data, i);
                fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer, enumMapping);
            }
        } else {
            final int[] tempBuffer = new int[dims[dims.length - 1]];
            buffer.get(tempBuffer);
            // Convert to enum values
            String[] stringData = (String[]) data;
            for (int i = 0; i < tempBuffer.length; i++) {
                stringData[i] = enumMapping.get(Math.toIntExact(Integer.toUnsignedLong(tempBuffer[i])));
            }
        }
    }

    private static void fillDataUnsigned(Object data, int[] dims, LongBuffer buffer, Map<Integer, String> enumMapping) {
        if (dims.length > 1) {
            for (int i = 0; i < dims[0]; i++) {
                Object newArray = Array.get(data, i);
                fillDataUnsigned(newArray, stripLeadingIndex(dims), buffer, enumMapping);
            }
        } else {
            final long[] tempBuffer = new long[dims[dims.length - 1]];
            final ByteBuffer tempByteBuffer = ByteBuffer.allocate(8);
            buffer.get(tempBuffer);
            // Convert to enum values
            String[] stringData = (String[]) data;
            for (int i = 0; i < tempBuffer.length; i++) {
                tempByteBuffer.putLong(0, tempBuffer[i]);
                stringData[i] = enumMapping.get((new BigInteger(1, tempByteBuffer.array())).intValueExact());
            }
        }
    }

    private static int[] stripLeadingIndex(int[] dims) {
        return Arrays.copyOfRange(dims, 1, dims.length);
    }
}
