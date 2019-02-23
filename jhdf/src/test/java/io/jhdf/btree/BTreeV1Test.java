package io.jhdf.btree;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.Superblock;
import io.jhdf.exceptions.HdfException;

public class BTreeV1Test {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@BeforeEach
	void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("../test_chunked_datasets_earliest.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
	}

	@AfterEach
	void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	void testGroupBTreeNode() {
		BTreeV1Group bTree = BTreeV1.createGroupBTree(fc, sb, 136);

		assertThat(bTree.getAddress(), is(equalTo(136L)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(1)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), hasSize(1));
		assertThat(bTree.getChildAddresses(), contains(1504L));
	}

	@Test
	void testDataBTreeNode() {
		BTreeV1Data bTree = BTreeV1.createDataBTree(fc, sb, 2104, 2);

		assertThat(bTree.getAddress(), is(equalTo(2104L)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(20)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), hasSize(20));
		assertThat(bTree.getChunks(), hasSize(20));
	}

	@Test
	void testCreatingBTreeOfDataTypeWithGroupThrows() {
		assertThrows(HdfException.class, () -> BTreeV1.createDataBTree(fc, sb, 136, 1245));
	}

	@Test
	void testCreatingBTreeOfGroupTypeWithDataThrows() {
		assertThrows(HdfException.class, () -> BTreeV1.createGroupBTree(fc, sb, 2104));
	}
}
