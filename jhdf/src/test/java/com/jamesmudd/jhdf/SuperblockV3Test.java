package com.jamesmudd.jhdf;

import static com.jamesmudd.jhdf.Utils.UNDEFINED_ADDRESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jamesmudd.jhdf.Superblock.SuperblockV2V3;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class SuperblockV3Test {
	private FileChannel fc;
	private RandomAccessFile raf;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file2.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testExtractV0SuperblockFromFile() throws IOException {
		Superblock sb = Superblock.readSuperblock(fc, 0);
		// Test version independent methods
		assertThat(sb.getVersionOfSuperblock(), is(equalTo(3)));
		assertThat(sb.getSizeOfOffsets(), is(equalTo(8)));
		assertThat(sb.getSizeOfLengths(), is(equalTo(8)));
		assertThat(sb.getBaseAddressByte(), is(equalTo(0L)));
		assertThat(sb.getEndOfFileAddress(), is(equalTo(raf.length())));

		// Test V3 only methods
		SuperblockV2V3 sbV3 = (SuperblockV2V3) sb;
		assertThat(sbV3.getSuperblockExtensionAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(sbV3.getRootGroupObjectHeaderAddress(), is(equalTo(48L)));
//		assertThat(sbV0.getVersionOfSharedHeaderMessageFormat(), is(equalTo(0)));
//		assertThat(sbV0.getGroupLeafNodeK(), is(equalTo(4)));
//		assertThat(sbV0.getGroupInternalNodeK(), is(equalTo(16)));
//		assertThat(sbV0.getAddressOfGlobalFreeSpaceIndex(), is(equalTo(UNDEFINED_ADDRESS)));
	}

	@Test
	public void testVerifySuperblock() throws Exception {
		assertThat(Superblock.verifySignature(fc, 0), is(true));
	}

	@Test
	public void testVerifySuperblockReturnsFalseWhenNotCorrect() throws Exception {
		assertThat(Superblock.verifySignature(fc, 3), is(false));
	}

	@Test(expected = HdfException.class)
	public void testReadSuperblockThrowsWhenGivenInvalidOffset() throws Exception {
		Superblock.readSuperblock(fc, 5);
	}
}
