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

import com.jamesmudd.jhdf.ObjectHeader.ObjectHeaderV2;

public class ObjectHeaderV2Test {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file2.hdf5").getFile();
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
	public void testRootGroupObjectHeaderV2() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 48); // Root group header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(48L)));
		assertThat(oh.getMessages().size(), is(equalTo(4)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getBirthTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getChangeTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getModificationTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testDatasetsGroupObjectHeaderV2() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 195); // Root group header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(195L)));
		assertThat(oh.getMessages().size(), is(equalTo(9)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getBirthTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getChangeTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getModificationTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnFloat16Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 608); // float16 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(608L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getBirthTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getChangeTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getModificationTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnFloat32Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 892); // float32 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(892L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getBirthTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getChangeTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getModificationTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnFloat64Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 1176); // float64 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(1176L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getBirthTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getChangeTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getModificationTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnInt8Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 1655); // int8 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(1655L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getBirthTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getChangeTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getModificationTime(), is(equalTo(1544032461L)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

}
