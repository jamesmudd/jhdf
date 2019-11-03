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

import io.jhdf.Superblock.SuperblockV2V3;
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

class SuperblockV3Test {
	private FileChannel fc;
	private RandomAccessFile raf;

	@BeforeEach
    void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("/hdf5/test_file2.hdf5").getFile();
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
		assertThat(sb.getVersionOfSuperblock(), is(equalTo(3)));
		assertThat(sb.getSizeOfOffsets(), is(equalTo(8)));
		assertThat(sb.getSizeOfLengths(), is(equalTo(8)));
		assertThat(sb.getBaseAddressByte(), is(equalTo(0L)));
		assertThat(sb.getEndOfFileAddress(), is(equalTo(raf.length())));

		// Test V3 only methods
		SuperblockV2V3 sbV3 = (SuperblockV2V3) sb;
		assertThat(sbV3.getSuperblockExtensionAddress(), is(equalTo(Constants.UNDEFINED_ADDRESS)));
		assertThat(sbV3.getRootGroupObjectHeaderAddress(), is(equalTo(48L)));
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
