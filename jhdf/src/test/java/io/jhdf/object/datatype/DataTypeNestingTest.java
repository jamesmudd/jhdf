/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataTypeNestingTest {

	private static final int ARRAY_TYPE_BYTES = 20;
	private static final int FIXED_POINT_BYTES = 12;

	@Test
	void testNestedArrayDataTypeRoundTrip() {
		DataType dataType = DataType.readDataType(createNestedArrayDataType(8));

		assertThat(dataType, is(instanceOf(ArrayDataType.class)));
	}

	@Test
	void testTooDeeplyNestedArrayDataTypeThrowsHdfException() {
		HdfException exception = assertThrows(HdfException.class,
			() -> DataType.readDataType(createNestedArrayDataType(65)));

		assertThat(exception.getMessage(), containsString("maximum depth"));
	}

	private static ByteBuffer createNestedArrayDataType(int levels) {
		ByteBuffer buffer = ByteBuffer.allocate(levels * ARRAY_TYPE_BYTES + FIXED_POINT_BYTES)
			.order(LITTLE_ENDIAN);

		for (int i = 0; i < levels; i++) {
			buffer.put((byte) ((2 << 4) | 10)); // Array data class, version 2
			buffer.put(new byte[3]); // Class bits
			buffer.putInt(1); // Size
			buffer.put((byte) 1); // Number of dimensions
			buffer.put(new byte[3]); // Reserved
			buffer.putInt(1); // Dimension size
			buffer.putInt(0); // Permutation index
		}

		buffer.put((byte) ((1 << 4) | FixedPoint.CLASS_ID)); // Fixed point, version 1
		buffer.put(new byte[3]); // Class bits
		buffer.putInt(1); // Size
		buffer.putShort((short) 0); // Bit offset
		buffer.putShort((short) 8); // Bit precision

		buffer.flip();
		return buffer;
	}
}
