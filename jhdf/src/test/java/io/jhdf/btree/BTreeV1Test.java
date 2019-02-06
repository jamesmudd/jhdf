package io.jhdf.btree;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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

import io.jhdf.Superblock;

public class BTreeV1Test {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@BeforeEach
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("../test_file.hdf5").getFile();
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
	public void testBTreeNode() throws IOException {
		BTreeV1 bTree = BTreeV1.createBTree(fc, sb, 136);

		assertThat(bTree.getNodeType(), is(equalTo((short) 0)));
		assertThat(bTree.getNodeLevel(), is(equalTo((short) 0)));
		assertThat(bTree.getEntriesUsed(), is(equalTo(1)));
		assertThat(bTree.getLeftSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getRightSiblingAddress(), is(equalTo(UNDEFINED_ADDRESS)));
		assertThat(bTree.getChildAddresses(), contains(1504L));
		assertThat(bTree.toString(), is(equalTo("BTreeV1 [address=136, nodeType=0, nodeLevel=0]")));
	}

}
