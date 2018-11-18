package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

public class GroupSymbolTableNodeTest {
	private RandomAccessFile raf;

	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
	}

	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		GroupSymbolTableNode node = new GroupSymbolTableNode(raf, 1504, 8);

		assertThat(node.getVersion(), is(equalTo((short) 1)));
		assertThat(node.getNumberOfEntries(), is(equalTo((short) 1)));
		assertThat(node.getSymbolTableEntries().length, is(equalTo(1)));
		assertThat(node.toString(), is(equalTo(
				"GroupSymbolTableNode [address=0x5e0, version=1, numberOfEntries=1, symbolTableEntries=[SymbolTableEntry [address=0x5e8, linkNameOffset=8, objectHeaderAddress=0x320, cacheType=1, bTreeAddress=0x348, nameHeapAddress=0x568, linkValueoffset=-1]]]")));
	}
}
