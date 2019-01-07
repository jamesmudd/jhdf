package io.jhdf.object.message;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import org.junit.jupiter.api.Test;

class FillValueOldMessageTest {

	@Test
	void testCreatingAndReadingMessage() {
		// 4 bytes for size and 4 for a float
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putInt(4);
		bb.putFloat(123.45f);
		bb.rewind();

		FillValueOldMessage fillValueOldMessage = new FillValueOldMessage(bb);

		ByteBuffer fillValueBuffer = fillValueOldMessage.getFillValue();
		assertThat(fillValueBuffer.getFloat(), is(equalTo(123.45f)));

		// Check the buffer is read only
		assertThrows(ReadOnlyBufferException.class, () -> fillValueBuffer.putInt(12));
	}

}
