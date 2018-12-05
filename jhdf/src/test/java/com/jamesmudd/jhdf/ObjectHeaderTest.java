package com.jamesmudd.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ObjectHeaderTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testObjectHeaderOnGroup() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 800); // dataset_group header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(800L)));
		assertThat(oh.getReferenceCount(), is(equalTo(1)));
		assertThat(oh.getMessages().size(), is(equalTo(7)));
	}

	@Test
	public void testObjectHeaderOnFloat16Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 7272); // float16 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(7272L)));
		assertThat(oh.getReferenceCount(), is(equalTo(1)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
	}

	@Test
	public void testObjectHeaderOnFloat32Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 7872); // float32 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(7872L)));
		assertThat(oh.getReferenceCount(), is(equalTo(1)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
	}

	@Test
	public void testObjectHeaderOnFloat64Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 10240); // float64 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(10240L)));
		assertThat(oh.getReferenceCount(), is(equalTo(1)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
	}

	@Test
	public void testObjectHeaderOnInt8Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 11176); // int8 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(11176L)));
		assertThat(oh.getReferenceCount(), is(equalTo(1)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));
	}

}
