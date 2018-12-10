package com.jamesmudd.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jamesmudd.jhdf.Superblock.SuperblockV0V1;

public class GroupTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private SuperblockV0V1 sb;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = (SuperblockV0V1) Superblock.readSuperblock(fc, 0);
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testRootGroup() throws IOException {
		Group rootGroup = Group.createRootGroup(fc, sb, sb.getRootGroupSymbolTableAddress());
		assertThat(rootGroup.getPath(), is(equalTo("")));
		assertThat(rootGroup.toString(), is(equalTo("RootGroup [name=/, path=/, address=0x38]")));
		assertThat(rootGroup.isGroup(), is(true));
		assertThat(rootGroup.getChildren().keySet(), hasSize(1));
		assertThat(rootGroup.getName(), is(equalTo("/")));
	}

	@Test
	public void testGroup() throws IOException {
		Group rootGroup = Group.createRootGroup(fc, sb, sb.getRootGroupSymbolTableAddress());
		Group group = Group.createGroup(fc, sb, 1512, "datasets_group", rootGroup);
		assertThat(group.getPath(), is(equalTo("/datasets_group")));
		assertThat(group.toString(), is(equalTo("Group [name=datasets_group, path=/datasets_group, address=0x5e8]")));
		assertThat(group.isGroup(), is(true));
		assertThat(group.getChildren().keySet(), hasSize(2));
		assertThat(group.getName(), is(equalTo("datasets_group")));
	}
}
