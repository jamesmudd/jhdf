package io.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.jhdf.Superblock.SuperblockV0V1;

public class GroupTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private SuperblockV0V1 sb;

	private Group rootGroup;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = (SuperblockV0V1) Superblock.readSuperblock(fc, 0);

		rootGroup = mock(Group.class);
		when(rootGroup.getPath()).thenReturn("/");
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testGroup() throws IOException {
		GroupImpl group = GroupImpl.createGroup(fc, sb, 800, "datasets_group", rootGroup);
		assertThat(group.getPath(), is(equalTo("/datasets_group/")));
		assertThat(group.toString(), is(equalTo("Group [name=datasets_group, path=/datasets_group/, address=0x320]")));
		assertThat(group.isGroup(), is(true));
		assertThat(group.getChildren().keySet(), hasSize(2));
		assertThat(group.getName(), is(equalTo("datasets_group")));
		assertThat(group.getType(), is(equalTo("Group")));
		assertThat(group.getParent(), is(rootGroup));
	}
}
