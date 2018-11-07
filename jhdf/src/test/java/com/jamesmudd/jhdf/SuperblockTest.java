package com.jamesmudd.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

public class SuperblockTest {

	private RandomAccessFile raf;
	
	@Before
	public void setup() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
	}
	
	@Test
	public void testExtractSuperblockFromFile() throws IOException {
		Superblock sb = new Superblock(raf);
		assertThat(sb.getVersionOfSuperblock(), is(equalTo(0)));
		assertThat(sb.getVersionNumberOfTheFileFreeSpaceInformation(), is(equalTo(0)));
		assertThat(sb.getVersionOfRootGroupSymbolTableEntry(), is(equalTo(0)));
		assertThat(sb.getVersionOfSharedHeaderMessageFormat(), is(equalTo(0)));
		assertThat(sb.getSizeOfOffsets(), is(equalTo(8)));
		assertThat(sb.getSizeOfLengths(), is(equalTo(8)));
		assertThat(sb.getGroupLeafNodeK(), is(equalTo(4)));
//		assertThat(sb.getBaseAddressByte(), is(equalTo(16)));
//		assertThat(sb.getAddressOfGlobalFreeSpaceIndex(), is(equalTo(8)));
		assertThat(sb.getEndOfFileAddress(), is(equalTo(raf.length())));
		assertThat(sb.getRootGroupSymbolTableAddress(), is(equalTo(56L)));



//
//		public long getDriverInformationBlockAddress() {
//			return driverInformationBlockAddress;
//		}
//
//		public long getRootGroupSymbolTableEntry() {
//			return rootGroupSymbolTableEntry;
//		}

	}

}
