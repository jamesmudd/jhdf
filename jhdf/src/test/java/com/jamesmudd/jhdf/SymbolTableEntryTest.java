package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

public class SymbolTableEntryTest {
	private RandomAccessFile raf;
	
	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
	}
	
	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		Superblock sb = new Superblock(raf);
		
		SymbolTableEntry ste = new SymbolTableEntry(raf, sb.getRootGroupSymbolTableAddress(), sb.getSizeOfOffsets());
	}
}
