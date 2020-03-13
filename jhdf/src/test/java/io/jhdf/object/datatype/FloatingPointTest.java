package io.jhdf.object.datatype;

import io.jhdf.exceptions.HdfTypeException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.powermock.reflect.Whitebox.newInstance;
import static org.powermock.reflect.Whitebox.setInternalState;

public class FloatingPointTest {

    private final int[] dims = new int[] { 2, 3 };

    // Float
    private final ByteBuffer floatBuffer = createFloatBuffer(new float[] { 1, 2, 3, 4, 5, 6 });
    private final FloatingPoint floatDataType = mockFloatingPoint(Float.BYTES);
    private final float[][] floatResult = new float[][] { { 1, 2, 3 }, { 4, 5, 6 } };

    // Double
    private final ByteBuffer doubleBuffer = createDoubleBuffer(new double[] { 1, 2, 3, 4, 5, 6 });
    private final FloatingPoint doubleDataType = mockFloatingPoint(Double.BYTES);
    private final double[][] doubleResult = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };

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
            Object actual = dataType.fillData(dims, buffer);
            assertThat(actual, is(expected));
        };
    }

    @Test
	void testUnsupportedFloatingPointLengthThrows() {
		FloatingPoint invalidDataType = mockFloatingPoint(11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> invalidDataType.fillData(dims, floatBuffer));
	}
}
