package io.jhdf.object.message;

import io.jhdf.Superblock;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.BitSet;

import static org.assertj.core.api.Assertions.assertThat;
class GroupInfoMessageTest {
	
	@Test
	void roundtrip() {
		GroupInfoMessage groupInfoMessage = GroupInfoMessage.createBasic();

		ByteBuffer buffer = groupInfoMessage.toBuffer();

		GroupInfoMessage roundtripMessage = new GroupInfoMessage(buffer, new BitSet(1));

		assertThat(roundtripMessage).usingRecursiveComparison()
			.withStrictTypeChecking()
			.isEqualTo(groupInfoMessage);
	}

}
