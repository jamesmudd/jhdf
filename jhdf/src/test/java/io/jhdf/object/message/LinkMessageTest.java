package io.jhdf.object.message;

import io.jhdf.Superblock;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
class LinkMessageTest {

	@Test
	void roundtrip() {
		LinkMessage linkMessage = LinkMessage.create("linkName", 12345);

		ByteBuffer buffer = linkMessage.toBuffer();

		LinkMessage linkMessageReadBack = new LinkMessage(buffer, new Superblock.SuperblockV2V3(), null);
//		assertThat.
	}
}
