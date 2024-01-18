package io.jhdf.object.message;

import io.jhdf.Superblock;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.BitSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LinkInfoMessageTest {

	@Test
	void roundtrip() {
		LinkInfoMessage linkInfoMessage = LinkInfoMessage.createBasic();

		ByteBuffer buffer = linkInfoMessage.toBuffer();

		LinkInfoMessage roundtripMessage = new LinkInfoMessage(buffer, new Superblock.SuperblockV2V3(), new BitSet(1));

		assertThat(roundtripMessage).usingRecursiveComparison()
			.withStrictTypeChecking()
			.isEqualTo(linkInfoMessage);
	}

}
