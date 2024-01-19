/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.object.message;

import io.jhdf.Superblock;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.BitSet;

import static org.assertj.core.api.Assertions.assertThat;

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
