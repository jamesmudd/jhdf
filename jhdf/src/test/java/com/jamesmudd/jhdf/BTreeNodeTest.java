package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;


public class BTreeNodeTest {
	private RandomAccessFile raf;
	
	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
	}
	
	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		BTreeNode bTree = new BTreeNode(raf, 136, 8, 8, 4, 16);
		
		assertThat(bTree.getNodeType(),	is(equalTo((short) 0)));
		assertThat(bTree.getNodeLevel(), is(equalTo((short) 0)));
		assertThat(bTree.getEntriesUsed(), is(equalTo((short) 256)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(-1L)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(-1L)));
	}
}
