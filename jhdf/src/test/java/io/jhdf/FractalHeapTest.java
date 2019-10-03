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

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.exceptions.HdfException;

public class FractalHeapTest {

	private FractalHeap fractalHeap;

	@BeforeEach
	void setup() throws IOException {
		String testFile = this.getClass().getResource("test_large_group_latest.hdf5").getFile();
		try (RandomAccessFile raf = new RandomAccessFile(new File(testFile), "r")) {
			FileChannel fc = raf.getChannel();
			Superblock sb = Superblock.readSuperblock(fc, 0);
			HdfFileChannel hdfFc = new HdfFileChannel(fc, sb);

			fractalHeap = new FractalHeap(hdfFc, 1870);
		}
	}

	@Test
	void testGetIdWorksForValidId() {
		ByteBuffer id = ByteBuffer.allocate(7);
		id.put(new byte[] { 0 }); // Flags none set for managed
		id.putInt(3129); // offset
		id.putShort((short) 18); // length
		id.rewind();

		ByteBuffer data = fractalHeap.getId(id);

		assertThat(data.position(), is(equalTo(0)));
		assertThat(data.limit(), is(equalTo(18)));
	}

	@Test
	void testWrongIdLengthThrows() {
		ByteBuffer bb = ByteBuffer.allocate(12); // This fractal heap needs 7 byte IDs
		assertThrows(HdfException.class, () -> fractalHeap.getId(bb));
	}

	@Test
	void testGetters() {
		assertThat(fractalHeap.getAddress(), is(equalTo(1870L)));
		assertThat(fractalHeap.getAddressOfManagedBlocksFreeSpaceManager(), is(equalTo(5270L)));
		assertThat(fractalHeap.getAmountOfAllocatedManagedSpaceInHeap(), is(equalTo(20480L)));
		assertThat(fractalHeap.getAmountOfManagedSpaceInHeap(), is(equalTo(262144L)));
		assertThat(fractalHeap.getBTreeAddressOfHugeObjects(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(fractalHeap.getFreeSpaceInManagedBlocks(), is(equalTo(243582L)));
		assertThat(fractalHeap.getIdLength(), is(equalTo(7)));
		assertThat(fractalHeap.getIoFiltersLength(), is(equalTo(0)));
		assertThat(fractalHeap.getMaxDirectBlockSize(), is(equalTo(65536)));
		assertThat(fractalHeap.getMaxSizeOfManagedObjects(), is(equalTo(4096L)));
		assertThat(fractalHeap.getNextHugeObjectId(), is(equalTo(0L)));
		assertThat(fractalHeap.getNumberOfHugeObjectsInHeap(), is(equalTo(0L)));
		assertThat(fractalHeap.getSizeOfHugeObjectsInHeap(), is(equalTo(0L)));
		assertThat(fractalHeap.getNumberOfManagedObjectsInHeap(), is(equalTo(1000L)));
		assertThat(fractalHeap.getNumberOfTinyObjectsInHeap(), is(equalTo(0L)));
		assertThat(fractalHeap.getSizeOfTinyObjectsInHeap(), is(equalTo(0L)));
		assertThat(fractalHeap.getStartingRowsInRootIndirectBlock(), is(equalTo(1)));
		assertThat(fractalHeap.getOffsetOfDirectBlockAllocationIteratorInManagedSpace(), is(equalTo(20480L)));
	}

	@Test
	void testToString() {
		assertThat(fractalHeap.toString(), is(equalTo(
				"FractalHeap [address=1870, idLength=7, numberOfTinyObjectsInHeap=0, numberOfHugeObjectsInHeap=0, numberOfManagedObjectsInHeap=1000]")));
	}

}
