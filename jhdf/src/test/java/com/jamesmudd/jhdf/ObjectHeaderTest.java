package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;


public class ObjectHeaderTest {
	private FileChannel	fc;
	
	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		fc = new RandomAccessFile(new File(testFileUrl), "r").getChannel();
	}
	
	@Test
	public void testObjectHeaderOnFloatDataset() throws IOException {
		ObjectHeader oh = new ObjectHeader(fc, 2864);
		
//		assertThat(heap.getVersion(),	is(equalTo((short) 0)));
//		assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
//		assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(24L)));
//		assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
	}
}
