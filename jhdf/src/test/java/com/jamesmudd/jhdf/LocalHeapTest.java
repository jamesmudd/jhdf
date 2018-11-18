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

public class LocalHeapTest {
	private FileChannel	fc;
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

	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		LocalHeap heap = new LocalHeap(raf, 680, 8, 8);

		assertThat(heap.getVersion(), is(equalTo((short) 0)));
		assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
		assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(24L)));
		assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
		assertThat(heap.toString(), is(equalTo(
				"LocalHeap [address=0x2a8, version=0, dataSegmentSize=88, offsetToHeadOfFreeList=24, addressOfDataSegment=0x2c8]")));
	}
}
