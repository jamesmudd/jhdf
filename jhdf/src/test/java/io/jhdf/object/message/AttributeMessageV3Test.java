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
import io.jhdf.TestUtils;
import io.jhdf.storage.HdfBackingStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class AttributeMessageV3Test {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;
	private ByteBuffer bb;
	private HdfBackingStorage hdfBackingStorage;

	@BeforeEach
	void setUp() throws IOException {
		File testFile = TestUtils.getTestFile("test_file2.hdf5");
		raf = new RandomAccessFile(testFile, "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
		bb = fc.map(READ_ONLY, 270, 65);
		bb.order(LITTLE_ENDIAN);
		hdfBackingStorage = Mockito.mock(HdfBackingStorage.class);
		Mockito.when(hdfBackingStorage.getSuperblock()).thenReturn(sb);
	}

	@AfterEach
	void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	void test() throws CharacterCodingException {
		AttributeMessage am = new AttributeMessage(bb, hdfBackingStorage, BitSet.valueOf(new byte[1]));
		assertThat(am.getVersion(), is(equalTo(3)));
		assertThat(am.getName(), is(equalTo("string_attr")));
		assertThat(am.getDataType().getDataClass(), is(equalTo(9)));
		assertThat(am.getDataSpace().getTotalLength(), is(equalTo(1L)));
		assertThat(am.getDataBuffer().capacity(), is(equalTo(16)));

		StandardCharsets.UTF_8.newDecoder().decode(am.getDataBuffer());
	}

}
