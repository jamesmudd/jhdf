package io.jhdf;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class BufferBuilderTest {

	@Test
	void writeIntsAsUnsignedByte() {

		int i = 255;

		ByteBuffer buffer = new BufferBuilder()
				.writeByte(i)
				.writeByte(i)
				.build();

		assertThat(Byte.toUnsignedInt(buffer.get()), is(255));
		assertThat(Byte.toUnsignedInt(buffer.get()), is(255));
	}

	@Test
	void writeBytes() {

		byte[] bytes = new byte[]{ Byte.MIN_VALUE, -2, -1, 0, 1, 2, Byte.MAX_VALUE };

		ByteBuffer buffer = new BufferBuilder()
				.writeBytes(bytes)
				.build();

		byte[] returnedBytes = new byte[bytes.length];
		buffer.get(returnedBytes);

		assertThat(returnedBytes, is(equalTo(bytes)));
	}
}