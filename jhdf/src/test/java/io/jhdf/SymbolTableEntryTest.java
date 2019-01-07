package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SymbolTableEntryTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@BeforeEach
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
	}

	@AfterEach
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testSymbolTableEntry() throws IOException {
		SymbolTableEntry ste = new SymbolTableEntry(fc, 56, sb);
		assertThat(ste.getLinkNameOffset(), is(equalTo(0)));
		assertThat(ste.getObjectHeaderAddress(), is(equalTo(96L)));
		assertThat(ste.getCacheType(), is(equalTo(1)));
		assertThat(ste.getBTreeAddress(), is(equalTo(136L)));
		assertThat(ste.getNameHeapAddress(), is(equalTo(680L)));
		assertThat(ste.toString(), is(equalTo(
				"SymbolTableEntry [address=0x38, linkNameOffset=0, objectHeaderAddress=0x60, cacheType=1, bTreeAddress=0x88, nameHeapAddress=0x2a8, linkValueOffset=-1]")));
	}
}
