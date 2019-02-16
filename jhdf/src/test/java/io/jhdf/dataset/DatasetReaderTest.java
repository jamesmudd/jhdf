package io.jhdf.dataset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import io.jhdf.dataset.DatasetReader;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.FixedPoint;
import io.jhdf.object.datatype.FloatingPoint;

class DatasetReaderTest {

	private int[] dims = new int[] { 2, 3 };

	// Byte
	private ByteBuffer byteBuffer = createByteBuffer(new byte[] { 1, -2, 3, -4, 5, -6 });
	private DataType byteDataType = mockFixedPoint(byte.class, true, Byte.BYTES);
	private DataType unsignedByteDataType = mockFixedPoint(int.class, false, Byte.BYTES);
	private byte[][] byteResult = new byte[][] { { 1, -2, 3 }, { -4, 5, -6 } };
	private int[][] unsignedByteResult = new int[][] { { 1, 254, 3 }, { 252, 5, 250 } };

	// Short
	private ByteBuffer shortBuffer = createShortBuffer(new short[] { 1, -2, 3, -4, 5, -6 });
	private DataType shortDataType = mockFixedPoint(short.class, true, Short.BYTES);
	private DataType unsignedShortDataType = mockFixedPoint(int.class, false, Short.BYTES);
	private short[][] shortResult = new short[][] { { 1, -2, 3 }, { -4, 5, -6 } };
	private int[][] unsignedShortResult = new int[][] { { 1, 65534, 3 }, { 65532, 5, 65530 } };

	// Int
	private ByteBuffer intBuffer = createIntBuffer(new int[] { 1, -2, 3, -4, 5, -6 });
	private DataType intDataType = mockFixedPoint(int.class, true, Integer.BYTES);
	private DataType unsignedIntDataType = mockFixedPoint(long.class, false, Integer.BYTES);
	private int[][] intResult = new int[][] { { 1, -2, 3 }, { -4, 5, -6 } };
	private long[][] unsignedIntResult = new long[][] { { 1L, 4294967294L, 3L }, { 4294967292L, 5L, 4294967290L } };

	// Long
	private ByteBuffer longBuffer = createLongBuffer(new long[] { 1L, 2L, 3L, 4L, 5L, 6L });
	private DataType longDataType = mockFixedPoint(long.class, true, Long.BYTES);
	private DataType unsignedLongDataType = mockFixedPoint(BigInteger.class, false, Long.BYTES);
	private long[][] longResult = new long[][] { { 1L, 2L, 3L }, { 4L, 5L, 6L } };
	private BigInteger[][] unsignedLongResult = createUnsignedLongResult();

	// Float
	private ByteBuffer floatBuffer = createFloatBuffer(new float[] { 1, 2, 3, 4, 5, 6 });
	private DataType floatDataType = mockFloatingPoint(float.class, true, Float.BYTES);
	private float[][] floatResult = new float[][] { { 1, 2, 3 }, { 4, 5, 6 } };

	// Double
	private ByteBuffer doubleBuffer = createDoubleBuffer(new double[] { 1, 2, 3, 4, 5, 6 });
	private DataType doubleDataType = mockFloatingPoint(double.class, true, Double.BYTES);
	private double[][] doubleResult = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };

	@TestFactory
	Collection<DynamicNode> datasetReadTests() throws Exception {
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
				dynamicTest("Double", createTest(doubleBuffer, doubleDataType, dims, doubleResult)));
	}

	@Test
	void testUnsupportedFixedPointLentghThrows() throws Exception {
		DataType invalidDataType = mockFixedPoint(int.class, true, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	@Test
	void testUnsupportedUnsignedFixedPointLentghThrows() throws Exception {
		DataType invalidDataType = mockFixedPoint(int.class, false, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	@Test
	void testUnsupportedFloatingPointLentghThrows() throws Exception {
		DataType invalidDataType = mockFloatingPoint(double.class, true, 11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> DatasetReader.readDataset(invalidDataType, longBuffer, dims));
	}

	private BigInteger[][] createUnsignedLongResult() {
		return new BigInteger[][] { { BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3) },
				{ BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6) } };
	}

	private Executable createTest(ByteBuffer buffer, DataType dataType, int[] dims, Object expected) {
		return new Executable() {
			@Override
			public void execute() throws Throwable {
				buffer.rewind(); // For shared buffers
				Object actual = DatasetReader.readDataset(dataType, buffer, dims);
				verifyArray(actual, expected);
			}
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
	private FloatingPoint mockFloatingPoint(@SuppressWarnings("rawtypes") Class javaType, boolean signed, int size) {
		FloatingPoint floatingPoint = mock(FloatingPoint.class);
		when(floatingPoint.getJavaType()).thenReturn(javaType);
		when(floatingPoint.getSize()).thenReturn(size);
		when(floatingPoint.getByteOrder()).thenReturn(ByteOrder.nativeOrder());
		return floatingPoint;
	}

	private void verifyArray(Object actual, Object expected) {
		assertThat(actual, Matchers.is(Matchers.equalTo(expected)));
	}
}
