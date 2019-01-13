package io.jhdf.object.message;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;
import java.util.BitSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.Constants;

class ObjectCommentMessageTest {

	private final String comment = "Test object comment";
	private ByteBuffer buffer;
	private BitSet flags = BitSet.valueOf(new byte[1]); // Empty flags

	@BeforeEach
	private void createBuffer() {
		byte[] bytes = comment.getBytes();
		buffer = ByteBuffer.allocate(bytes.length + 1);
		buffer.rewind();
		buffer.put(bytes);
		buffer.put(Constants.NULL); // Null terminated
		buffer.rewind();
	}

	@Test
	void testObjectModificationTimeMessage() {
		ObjectCommentMessage message = new ObjectCommentMessage(buffer, flags);
		assertThat(message.getComment(), is(equalTo(comment)));
	}

}
