/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;
import net.jpountz.lz4.LZ4Factory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class Lz4FilterTest {

	@Test
	void testLazyInitFailure() {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		buffer.putLong(64); // Total size
		buffer.putInt(16); // Block size
		buffer.putInt(8); // first block size
		byte[] bytes = buffer.array();

		try(MockedStatic<LZ4Factory> lz4FactoryMock = mockStatic(LZ4Factory.class)) {
			lz4FactoryMock.when(LZ4Factory::fastestJavaInstance).thenThrow(new RuntimeException("test"));
			Lz4Filter lz4Filter = new Lz4Filter();
			assertThrows(HdfFilterException.class, () -> lz4Filter.decode(bytes, new int[0]));
		}
	}
}
