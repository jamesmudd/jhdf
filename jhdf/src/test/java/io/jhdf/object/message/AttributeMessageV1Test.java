/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class AttributeMessageV1Test {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;
	private ByteBuffer bb;

	@BeforeEach
	public void setUp() throws IOException {
		final String testFileUrl = this.getClass().getResource("../../test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
		bb = fc.map(READ_ONLY, 1864, 80);
		bb.order(LITTLE_ENDIAN);
	}

	@AfterEach
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void test() {
		AttributeMessage am = new AttributeMessage(bb, sb, BitSet.valueOf(new byte[1]));
		assertThat(am.getName(), is(equalTo("string_attr")));
		assertThat(am.getDataType().getDataClass(), is(equalTo(9)));
		assertThat(am.getDataSpace().getTotalLength(), is(equalTo(1L)));
		assertThat(am.getDataBuffer().capacity(), is(equalTo(16)));
		assertThat(am.getVersion(), is(equalTo(1)));
	}

}
