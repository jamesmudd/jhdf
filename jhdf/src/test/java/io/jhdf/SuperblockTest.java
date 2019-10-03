/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.Superblock.SuperblockV0V1;
import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuperblockTest {
	private FileChannel fc;
	private RandomAccessFile raf;

	@BeforeEach
    void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
	}

	@AfterEach
    void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
    void testExtractV0SuperblockFromFile() throws IOException {
		Superblock sb = Superblock.readSuperblock(fc, 0);
		// Test version independent methods
		assertThat(sb.getVersionOfSuperblock(), is(equalTo(0)));
		assertThat(sb.getSizeOfOffsets(), is(equalTo(8)));
		assertThat(sb.getSizeOfLengths(), is(equalTo(8)));
		assertThat(sb.getBaseAddressByte(), is(equalTo(0L)));
		assertThat(sb.getEndOfFileAddress(), is(equalTo(raf.length())));

		// Test V0 only methods
		SuperblockV0V1 sbV0 = (SuperblockV0V1) sb;
		assertThat(sbV0.getVersionNumberOfTheFileFreeSpaceInformation(), is(equalTo(0)));
		assertThat(sbV0.getVersionOfRootGroupSymbolTableEntry(), is(equalTo(0)));
		assertThat(sbV0.getVersionOfSharedHeaderMessageFormat(), is(equalTo(0)));
		assertThat(sbV0.getGroupLeafNodeK(), is(equalTo(4)));
		assertThat(sbV0.getGroupInternalNodeK(), is(equalTo(16)));
		assertThat(sbV0.getAddressOfGlobalFreeSpaceIndex(), is(equalTo(Constants.UNDEFINED_ADDRESS)));
		assertThat(sbV0.getDriverInformationBlockAddress(), is(equalTo(Constants.UNDEFINED_ADDRESS)));
		assertThat(sbV0.getRootGroupSymbolTableAddress(), is(equalTo(56L)));
	}

	@Test
    void testVerifySuperblock() {
		assertThat(Superblock.verifySignature(fc, 0), is(true));
	}

	@Test
    void testVerifySuperblockReturnsFalseWhenNotCorrect() {
		assertThat(Superblock.verifySignature(fc, 3), is(false));
	}

	@Test
    void testReadSuperblockThrowsWhenGivenInvalidOffset() {
		assertThrows(HdfException.class, () -> Superblock.readSuperblock(fc, 5));
	}
}
