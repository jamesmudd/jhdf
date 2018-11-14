package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;


public class SymbolTableEntryTest {
	private RandomAccessFile raf;
	
	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
	}
	
	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		SymbolTableEntry ste = new SymbolTableEntry(raf, 56, 8);
		assertThat(ste.getLinkNameOffset(),	is(equalTo(0L)));
		assertThat(ste.getObjectHeaderAddress(), is(equalTo(96L)));
		assertThat(ste.getCacheType(), is(equalTo(1)));
		assertThat(ste.getbTreeAddress(), is(equalTo(136L)));
		assertThat(ste.getNameHeapAddress(), is(equalTo(680L)));		
	}
}
