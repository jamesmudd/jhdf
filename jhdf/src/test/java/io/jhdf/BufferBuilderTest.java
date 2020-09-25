package io.jhdf;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BufferBuilderTest {

	@Test
	void writeIntsAsUnsignedByte() {

		int i = 255;

		ByteBuffer buffer = new BufferBuilder()
				.writeByte(i)
				.writeByte(i)
				.build();

		assertThat(Byte.toUnsignedInt(buffer.get()), Matchers.is(255));
		assertThat(Byte.toUnsignedInt(buffer.get()), Matchers.is(255));
	}
}