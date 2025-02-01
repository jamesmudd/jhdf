/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;
import io.jhdf.api.WritableAttributeImpl;
import io.jhdf.storage.HdfBackingStorage;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttributeMessageTest {

	@Test
	void roundtrip() {
		WritableAttributeImpl testAttr = new WritableAttributeImpl("test_attr", null, new int[]{1, 2, 3});
		AttributeMessage attributeMessage = AttributeMessage.create("test_attr", testAttr);

		ByteBuffer buffer = attributeMessage.toBuffer();

		Superblock superblockMock= mock(Superblock.class);
		when(superblockMock.getSizeOfOffsets()).thenReturn(8);
		when(superblockMock.getSizeOfLengths()).thenReturn(8);
		HdfBackingStorage hdfBackingStorageMock = mock(HdfBackingStorage.class);
		when(hdfBackingStorageMock.getSuperblock()).thenReturn(superblockMock);
		AttributeMessage roundtripMessage = new AttributeMessage(buffer, hdfBackingStorageMock, new BitSet(1));

		assertThat(roundtripMessage).usingRecursiveComparison()
			.withStrictTypeChecking()
			.isEqualTo(attributeMessage);

		IntBuffer readBackData = roundtripMessage.getDataBuffer().asIntBuffer();
		assertThat(readBackData.get()).isEqualTo(1);
		assertThat(readBackData.get()).isEqualTo(2);
		assertThat(readBackData.get()).isEqualTo(3);
		assertThat(readBackData.limit()).isEqualTo(3);
	}

}
