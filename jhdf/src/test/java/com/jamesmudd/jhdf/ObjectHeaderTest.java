package com.jamesmudd.jhdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;

public class ObjectHeaderTest {
	private FileChannel fc;
	private RandomAccessFile raf;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	// @Test
	public void testObjectHeaderOnFloatDataset() throws IOException {
		ObjectHeader oh = new ObjectHeader(fc, 2864);

		// assertThat(heap.getVersion(), is(equalTo((short) 0)));
		// assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
		// assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(24L)));
		// assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
	}
}
