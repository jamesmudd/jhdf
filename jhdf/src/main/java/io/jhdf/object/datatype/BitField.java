/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.jhdf.exceptions.HdfTypeException;

public class BitField extends DataType implements OrderedDataType {
    private final ByteOrder order;
    private final boolean lowPadding;
    private final boolean highPadding;
    private final short bitOffset;
    private final short bitPrecision;

    public BitField(ByteBuffer bb) {
        super(bb);

        if (classBits.get(0)) {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            order = ByteOrder.LITTLE_ENDIAN;
        }

        lowPadding = classBits.get(1);
        highPadding = classBits.get(2);

        bitOffset = bb.getShort();
        bitPrecision = bb.getShort();
    }

    @Override
    public ByteOrder getByteOrder() {
        return order;
    }

    public boolean isLowPadding() {
        return lowPadding;
    }

    public boolean isHighPadding() {
        return highPadding;
    }

    public short getBitOffset() {
        return bitOffset;
    }

    public short getBitPrecision() {
        return bitPrecision;
    }

    @Override
    public Class<?> getJavaType() {
        switch (bitPrecision) {
        case 8:
            return byte.class;
        case 1:
            return boolean.class;
        default:
            throw new HdfTypeException("Unsupported bitfield data precision");
        }
    }
}
