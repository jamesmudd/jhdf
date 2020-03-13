package io.jhdf.object.datatype;

import io.jhdf.exceptions.HdfTypeException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.powermock.reflect.Whitebox.newInstance;
import static org.powermock.reflect.Whitebox.setInternalState;

public class ReferenceTest {

    private final int[] dims = new int[] { 2, 3 };

    private final Reference referenceDataType = mockReference(Long.BYTES);
    private final Reference referenceDataTypeSmall = mockReference(4);
    private final ByteBuffer referenceIntBuffer = FixedPointTest.createIntBuffer(new int[] { 1, 200, 3012, 414, 50, 666666 });
    private final long[][] referenceIntLongResult = new long[][] { { 1L, 200L, 3012L }, { 414L, 50L, 666666L } };

    private final ByteBuffer longBuffer = FixedPointTest.createLongBuffer(new long[] { 1L, 2L, 3L, 4L, 5L, 6L });
    private final long[][] longResult = new long[][] { { 1L, 2L, 3L }, { 4L, 5L, 6L } };

    private Reference mockReference(int sizeInBytes) {
        Reference floatingPoint = newInstance(Reference.class);
        setInternalState(floatingPoint, "size", sizeInBytes);
        return floatingPoint;
    }

    @TestFactory
    Collection<DynamicNode> datasetReadTests() {
		return Arrays.asList(
				dynamicTest("Reference8", createTest(longBuffer, referenceDataType, dims, longResult)),
				dynamicTest("Reference4", createTest(referenceIntBuffer, referenceDataTypeSmall, dims, referenceIntLongResult)));
	}

    private Executable createTest(ByteBuffer buffer, Reference dataType, int[] dims, Object expected) {
        return () -> {
            buffer.rewind(); // For shared buffers
            Object actual = dataType.fillData(dims, buffer);
            assertThat(actual, is(expected));
        };
    }

    @Test
	void testUnsupportedReferenceLengthThrows() {
		Reference invalidDataType = mockReference(11); // 11 byte data is not supported
		assertThrows(HdfTypeException.class, () -> invalidDataType.fillData(dims, referenceIntBuffer));
	}
}
