/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.storage.HdfFileChannel;
import io.jhdf.exceptions.HdfTypeException;
import io.jhdf.storage.HdfBackingStorage;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.powermock.reflect.Whitebox.newInstance;
import static org.powermock.reflect.Whitebox.setInternalState;

public class FloatingPointTest {

	private final int[] dims = new int[]{2, 3};

	// Float
	private final ByteBuffer floatBuffer = createFloatBuffer(new float[]{1, 2, 3, 4, 5, 6});
	private final FloatingPoint floatDataType = mockFloatingPoint(Float.BYTES);
	private final float[][] floatResult = new float[][]{{1, 2, 3}, {4, 5, 6}};

	// Double
	private final ByteBuffer doubleBuffer = createDoubleBuffer(new double[]{1, 2, 3, 4, 5, 6});
	private final FloatingPoint doubleDataType = mockFloatingPoint(Double.BYTES);
	private final double[][] doubleResult = new double[][]{{1, 2, 3}, {4, 5, 6}};

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

	private FloatingPoint mockFloatingPoint(int sizeInBytes) {
		FloatingPoint floatingPoint = newInstance(FloatingPoint.class);
		setInternalState(floatingPoint, "order", ByteOrder.nativeOrder());
		setInternalState(floatingPoint, "bitPrecision", (short) (sizeInBytes * 8));
		setInternalState(floatingPoint, "size", sizeInBytes);
		return floatingPoint;
	}

	@TestFactory
	Stream<DynamicNode> datasetReadTests() {
		return Stream.of(
			dynamicTest("Float", createTest(floatBuffer, floatDataType, dims, floatResult)),
			dynamicTest("Double", createTest(doubleBuffer, doubleDataType, dims, doubleResult)));
	}

	private Executable createTest(ByteBuffer buffer, FloatingPoint dataType, int[] dims, Object expected) {
		return () -> {
			buffer.rewind(); // For shared buffers
			HdfBackingStorage hdfBackingStorage = mock(HdfFileChannel.class);
			Object actual = dataType.fillData(buffer, dims, hdfBackingStorage);
			assertThat(actual, is(expected));
			verifyNoInteractions(hdfBackingStorage);
		};
	}

	@Test
	void testUnsupportedFloatingPointLengthThrows() {
		FloatingPoint invalidDataType = mockFloatingPoint(11); // 11 byte data is not supported
		HdfBackingStorage hdfBackingStorage = mock(HdfBackingStorage.class);
		assertThrows(HdfTypeException.class, () -> invalidDataType.fillData(floatBuffer, dims, hdfBackingStorage));
		verifyNoInteractions(hdfBackingStorage);
	}

	/**
	 * Examples from https://en.wikipedia.org/wiki/Half-precision_floating-point_format
	 *
	 * @return inputs to #testHalfPrecisionFloats
	 */
	static Stream<Arguments> testHalfPrecisionFloats() {
		return Stream.of(
			Arguments.of((short) 0x1400, 9.765625E-4f),
			Arguments.of((short) 0x1000, 4.8828125E-4f),
			Arguments.of((short) 0x0001, 5.9604645E-8f), // smallest positive subnormal number
			Arguments.of((short) 0x03FF, 6.097555E-5f), // largest subnormal number
			Arguments.of((short) 0x3BFF, 0.9995117f), // largest number less than one
			Arguments.of((short) 0x3C00, 1.0f), // one
			Arguments.of((short) 0x3C01, 1.0009766f), // smallest number larger than one
			Arguments.of((short) 0xFBFF, -65504.0f), // largest negative normal number
			Arguments.of((short) 0x7BFF, 65504.0f), // largest normal number
			Arguments.of((short) 0xC000, -2.0f), // -2
			Arguments.of((short) 0x0000, 0.0f), // zero
			Arguments.of((short) 0x8000, -0.0f), // -ve zero
			Arguments.of((short) 0xFFFF, Float.NaN), // NaN
			Arguments.of((short) 0x7C00, Float.POSITIVE_INFINITY), // inf
			Arguments.of((short) 0xFC00, Float.NEGATIVE_INFINITY) // -ve inf
		);
	}

	@ParameterizedTest
	@MethodSource
	void testHalfPrecisionFloats(short input, float expected) {
		assertThat(FloatingPoint.toFloat(input), is(expected));
	}
}
