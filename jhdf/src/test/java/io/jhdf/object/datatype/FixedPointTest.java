/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.storage.HdfFileChannel;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.storage.HdfBackingStorage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.powermock.reflect.Whitebox.newInstance;
import static org.powermock.reflect.Whitebox.setInternalState;

class FixedPointTest {

	private final int[] dims = new int[]{2, 3};

	// Byte
	private final ByteBuffer byteBuffer = createByteBuffer(new byte[]{1, -2, 3, -4, 5, -6});
	private final FixedPoint byteDataType = mockFixedPoint(true, Byte.BYTES);
	private final FixedPoint unsignedByteDataType = mockFixedPoint(false, Byte.BYTES);
	private final byte[][] byteResult = new byte[][]{{1, -2, 3}, {-4, 5, -6}};
	private final int[][] unsignedByteResult = new int[][]{{1, 254, 3}, {252, 5, 250}};

	// Short
	private final ByteBuffer shortBuffer = createShortBuffer(new short[]{1, -2, 3, -4, 5, -6});
	private final FixedPoint shortDataType = mockFixedPoint(true, Short.BYTES);
	private final FixedPoint unsignedShortDataType = mockFixedPoint(false, Short.BYTES);
	private final short[][] shortResult = new short[][]{{1, -2, 3}, {-4, 5, -6}};
	private final int[][] unsignedShortResult = new int[][]{{1, 65534, 3}, {65532, 5, 65530}};

	// Int
	private final ByteBuffer intBuffer = createIntBuffer(new int[]{1, -2, 3, -4, 5, -6});
	private final FixedPoint intDataType = mockFixedPoint(true, Integer.BYTES);
	private final FixedPoint unsignedIntDataType = mockFixedPoint(false, Integer.BYTES);
	private final int[][] intResult = new int[][]{{1, -2, 3}, {-4, 5, -6}};
	private final long[][] unsignedIntResult = new long[][]{{1L, 4294967294L, 3L}, {4294967292L, 5L, 4294967290L}};

	// Long
	private final ByteBuffer longBuffer = createLongBuffer(new long[]{1L, 2L, 3L, 4L, 5L, 6L});
	private final FixedPoint longDataType = mockFixedPoint(true, Long.BYTES);
	private final FixedPoint unsignedLongDataType = mockFixedPoint(false, Long.BYTES);
	private final long[][] longResult = new long[][]{{1L, 2L, 3L}, {4L, 5L, 6L}};
	private final BigInteger[][] unsignedLongResult = new BigInteger[][]{
		{BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3)},
		{BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(6)}};

	private FixedPoint mockFixedPoint(boolean signed, int sizeInBytes) {
		FixedPoint fixedPoint = newInstance(FixedPoint.class);
		setInternalState(fixedPoint, "order", ByteOrder.nativeOrder());
		setInternalState(fixedPoint, "signed", signed);
		setInternalState(fixedPoint, "bitPrecision", (short) (sizeInBytes * 8));
		setInternalState(fixedPoint, "size", sizeInBytes);
		return fixedPoint;
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

	static ByteBuffer createIntBuffer(int[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Integer.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (int i : array) {
			buffer.putInt(i);
		}
		buffer.rewind();
		return buffer;
	}

	static ByteBuffer createLongBuffer(long[] array) {
		ByteBuffer buffer = ByteBuffer.allocate(array.length * Long.BYTES);
		buffer.order(ByteOrder.nativeOrder());
		for (long i : array) {
			buffer.putLong(i);
		}
		buffer.rewind();
		return buffer;
	}

	@TestFactory
	Stream<DynamicNode> datasetReadTests() {
		return Stream.of(
			dynamicTest("Signed Byte", createTest(byteBuffer, byteDataType, dims, byteResult)),
			dynamicTest("Unsigned Byte", createTest(byteBuffer, unsignedByteDataType, dims, unsignedByteResult)),
			dynamicTest("Signed Short", createTest(shortBuffer, shortDataType, dims, shortResult)),
			dynamicTest("Unsigned Short", createTest(shortBuffer, unsignedShortDataType, dims, unsignedShortResult)),
			dynamicTest("Signed Int", createTest(intBuffer, intDataType, dims, intResult)),
			dynamicTest("Unsigned Int", createTest(intBuffer, unsignedIntDataType, dims, unsignedIntResult)),
			dynamicTest("Signed Long", createTest(longBuffer, longDataType, dims, longResult)),
			dynamicTest("Unsigned Long", createTest(longBuffer, unsignedLongDataType, dims, unsignedLongResult)));
	}

	private Executable createTest(ByteBuffer buffer, FixedPoint dataType, int[] dims, Object expected) {
		return () -> {
			buffer.rewind(); // For shared buffers
			HdfBackingStorage hdfBackingStorage = mock(HdfFileChannel.class);
			Object actual = dataType.fillData(buffer, dims, hdfBackingStorage);
			assertThat(actual, is(expected));
			verifyNoInteractions(hdfBackingStorage);
		};
	}

	@Test
	void testUnsupportedFixedPointLengthThrows() {
		FixedPoint invalidDataType = mockFixedPoint(true, 11); // 11 byte data is not supported
		HdfBackingStorage hdfBackingStorage = mock(HdfBackingStorage.class);
		assertThrows(HdfTypeException.class, () -> invalidDataType.fillData(longBuffer, dims, hdfBackingStorage));
		verifyNoInteractions(hdfBackingStorage);
	}

	@Test
	void testUnsupportedUnsignedFixedPointLengthThrows() {
		FixedPoint invalidDataType = mockFixedPoint(false, 11); // 11 byte data is not supported
		HdfBackingStorage hdfBackingStorage = mock(HdfBackingStorage.class);
		assertThrows(HdfTypeException.class, () -> invalidDataType.fillData(longBuffer, dims, hdfBackingStorage));
		verifyNoInteractions(hdfBackingStorage);
	}

	@Test
	void testWritingFixedPoint() {
		FixedPoint fixedPoint = new FixedPoint(4);
		ByteBuffer buffer = fixedPoint.toBuffer();
		FixedPoint readBack = new FixedPoint(buffer);

		Assertions.assertThat(readBack).usingRecursiveComparison()
			.withStrictTypeChecking()
			.isEqualTo(fixedPoint);
	}

}
