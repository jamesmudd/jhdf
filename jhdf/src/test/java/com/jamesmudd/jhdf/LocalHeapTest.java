package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;


public class LocalHeapTest {
	private RandomAccessFile raf;
	
	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
	}
	
	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		LocalHeap heap = new LocalHeap(raf, 680, 8, 8);
		
		assertThat(heap.getVersion(),	is(equalTo((short) 0)));
		assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
		assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(24L)));
		assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
	}
}
