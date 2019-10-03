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

import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.FixedPoint;
import io.jhdf.object.datatype.FloatingPoint;
import io.jhdf.object.datatype.Reference;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class DatasetReaderTest {

	private final int[] dims = new int[] { 2, 3 };

	// Byte
	private final ByteBuffer byteBuffer = createByteBuffer(new byte[] { 1, -2, 3, -4, 5, -6 });
	private final DataType byteDataType = mockFixedPoint(byte.class, true, Byte.BYTES);
	private final DataType unsignedByteDataType = mockFixedPoint(int.class, false, Byte.BYTES);
	private final byte[][] byteResult = new byte[][] { { 1, -2, 3 }, { -4, 5, -6 } };
	private final int[][] unsignedByteResult = new int[][] { { 1, 254, 3 }, { 252, 5, 250 } };

	// Short
	private final ByteBuffer shortBuffer = createShortBuffer(new short[] { 1, -2, 3, -4, 5, -6 });
	private final DataType shortDataType = mockFixedPoint(short.class, true, Short.BYTES);
	private final DataType unsignedShortDataType = mockFixedPoint(int.class, false, Short.BYTES);
	private final short[][] shortResult = new short[][] { { 1, -2, 3 }, { -4, 5, -6 } };
	private final int[][] unsignedShortResult = new int[][] { { 1, 65534, 3 }, { 65532, 5, 65530 } };

	// Int
	private final ByteBuffer intBuffer = createIntBuffer(new int[] { 1, -2, 3, -4, 5, -6 });
	private final DataType intDataType = mockFixedPoint(int.class, true, Integer.BYTES);
	private final DataType unsignedIntDataType = mockFixedPoint(long.class, false, Integer.BYTES);
	private final int[][] intResult = new int[][] { { 1, -2, 3 }, { -4, 5, -6 } };
	private final long[][] unsignedIntResult = new long[][] { { 1L, 4294967294L, 3L }, { 4294967292L, 5L, 4294967290L } };

	// Long
	private final ByteBuffer longBuffer = createLongBuffer(new long[] { 1L, 2L, 3L, 4L, 5L, 6L });
	private final DataType longDataType = mockFixedPoint(long.class, true, Long.BYTES);
	private final DataType unsignedLongDataType = mockFixedPoint(BigInteger.class, false, Long.BYTES);
	private final long[][] longResult = new long[][] { { 1L, 2L, 3L }, { 4L, 5L, 6L } };
	private final BigInteger[][] unsignedLongResult = createUnsignedLongResult();

	// Float
	private final ByteBuffer floatBuffer = createFloatBuffer(new float[] { 1, 2, 3, 4, 5, 6 });
	private final DataType floatDataType = mockFloatingPoint(float.class, Float.BYTES);
	private final float[][] floatResult = new float[][] { { 1, 2, 3 }, { 4, 5, 6 } };

	// Double
	private final ByteBuffer doubleBuffer = createDoubleBuffer(new double[] { 1, 2, 3, 4, 5, 6 });
	private final DataType doubleDataType = mockFloatingPoint(double.class, Double.BYTES);
	private final double[][] doubleResult = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };

	// Reference
	private final DataType referenceDataType = mockReference(long.class, Long.BYTES);
	private final DataType referenceDataTypeSmall = mockReference(long.class, 4);
	private final ByteBuffer referenceIntBuffer = createIntBuffer(new int[] { 1, 200, 3012, 414, 50, 666666 });
	private final long[][] referenceIntLongResult = new long[][] { { 1L, 200L, 3012L }, { 414L, 50L, 666666L } };

	@TestFactory
	Collection<DynamicNode> datasetReadTests() {
		return Arrays.asList(dynamicTest("Signed Byte", createTest(byteBuffer, byteDataType, dims, byteResult)),
				dynamicTest("Unsigned Byte", createTest(byteBuffer, unsignedByteDataType, dims, unsignedByteResult)),
				dynamicTest("Signed Short", createTest(shortBuffer, shortDataType, dims, shortResult)),
				dynamicTest("Unsigned Short",
						createTest(shortBuffer, unsignedShortDataType, dims, unsignedShortResult)),
				dynamicTest("Signed Int", createTest(intBuffer, intDataType, dims, intResult)),
				dynamicTest("Unsigned Int", createTest(intBuffer, unsignedIntDataType, dims, unsignedIntResult)),
				dynamicTest("Signed Long", createTest(longBuffer, longDataType, dims, longResult)),
				dynamicTest("Unsigned Long", createTest(longBuffer, unsignedLongDataType, dims, unsignedLongResult)),
				dynamicTest("Float", createTest(floatBuffer, floatDataType, dims, floatResult)),
				dynamicTest("Double", createTest(doubleBuffer, doubleDataType, dims, doubleResult)),
				dynamicTest("Reference8", createTest(longBuffer, referenceDataType, dims, longResult)),
				dynamicTest("Reference4", createTest(referenceIntBuffer, referenceDataTypeSmall, dims, referenceIntLongResult)));
	}

	@Test
	void testUnsupportedFixedPointLengthThrows() {
		DataType invalidDataType = mockFixedPoint(int.class, true, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	@Test
	void testUnsupportedUnsignedFixedPointLengthThrows() {
		DataType invalidDataType = mockFixedPoint(int.class, false, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	@Test
	void testUnsupportedFloatingPointLengthThrows() {
		DataType invalidDataType = mockFloatingPoint(double.class, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	@Test
	void testUnsupportedReferenceLengthThrows() {
		DataType invalidDataType = mockReference(long.class, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	private BigInteger[][] createUnsignedLongResult() {
		return new BigInteger[][] { { BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3) },
				{ BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6) } };
	}

	private Executable createTest(ByteBuffer buffer, DataType dataType, int[] dims, Object expected) {
		return () -> {
			buffer.rewind(); // For shared buffers
			Object actual = DatasetReader.readDataset(dataType, buffer, dims);
			verifyArray(actual, expected);
		};
	}

	private ByteBuffer createByteBuffer(byte[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length);
		buffer.order(ByteOrder.nativeOrder());
		for (byte i : array) {
			buffer.put(i);
		}
		buffer.rewind();
		return buffer;
	}

	private ByteBuffer createShortBuffer(short[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Short.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (short i : array) {
			buffer.putShort(i);
		}
		buffer.rewind();
		return buffer;
	}

	private ByteBuffer createIntBuffer(int[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Integer.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (int i : array) {
			buffer.putInt(i);
		}
		buffer.rewind();
		return buffer;
	}

	private ByteBuffer createLongBuffer(long[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Long.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (long i : array) {
			buffer.putLong(i);
		}
		buffer.rewind();
		return buffer;
	}

	private ByteBuffer createFloatBuffer(float[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Float.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (float i : array) {
			buffer.putFloat(i);
		}
		buffer.rewind();
		return buffer;
	}

	private ByteBuffer createDoubleBuffer(double[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Double.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (double i : array) {
			buffer.putDouble(i);
		}
		buffer.rewind();
		return buffer;
	}

	@SuppressWarnings("unchecked")
	private FixedPoint mockFixedPoint(@SuppressWarnings("rawtypes") Class javaType, boolean signed, int size) {
		FixedPoint fixedPoint = mock(FixedPoint.class);
		when(fixedPoint.getJavaType()).thenReturn(javaType);
		when(fixedPoint.isSigned()).thenReturn(signed);
		when(fixedPoint.getSize()).thenReturn(size);
		when(fixedPoint.getByteOrder()).thenReturn(ByteOrder.nativeOrder());
		return fixedPoint;
	}

	@SuppressWarnings("unchecked")
	private FloatingPoint mockFloatingPoint(@SuppressWarnings("rawtypes") Class javaType, int size) {
		FloatingPoint floatingPoint = mock(FloatingPoint.class);
		when(floatingPoint.getJavaType()).thenReturn(javaType);
		when(floatingPoint.getSize()).thenReturn(size);
		when(floatingPoint.getByteOrder()).thenReturn(ByteOrder.nativeOrder());
		return floatingPoint;
	}

	@SuppressWarnings("unchecked")
	private Reference mockReference(@SuppressWarnings("rawtypes") Class javaType, int size) {
		Reference floatingPoint = mock(Reference.class);
		when(floatingPoint.getJavaType()).thenReturn(javaType);
		when(floatingPoint.getSize()).thenReturn(size);
		return floatingPoint;
	}

	private void verifyArray(Object actual, Object expected) {
		assertThat(actual, Matchers.is(Matchers.equalTo(expected)));
	}
}
