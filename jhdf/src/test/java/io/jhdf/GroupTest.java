package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.Superblock.SuperblockV0V1;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;

public class GroupTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private SuperblockV0V1 sb;

	private Group rootGroup;

	@BeforeEach
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = (SuperblockV0V1) Superblock.readSuperblock(fc, 0);

		rootGroup = mock(Group.class);
		when(rootGroup.getPath()).thenReturn("/");
	}

	@AfterEach
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testGroup() throws IOException {
		Group group = GroupImpl.createGroup(fc, sb, 800, "datasets_group", rootGroup);
		assertThat(group.getPath(), is(equalTo("/datasets_group/")));
		assertThat(group.toString(), is(equalTo("Group [name=datasets_group, path=/datasets_group/, address=0x320]")));
		assertThat(group.isGroup(), is(true));
		assertThat(group.getChildren().keySet(), hasSize(2));
		assertThat(group.getName(), is(equalTo("datasets_group")));
		assertThat(group.getType(), is(equalTo(NodeType.GROUP)));
		assertThat(group.getParent(), is(rootGroup));
		assertThat(group.getAddress(), is(equalTo(800L)));
	}

	@Test
	void testGettingChildrenByName() throws Exception {
		Group group = GroupImpl.createGroup(fc, sb, 800, "datasets_group", rootGroup);
		Node child = group.getChild("int");
		assertThat(child, is(notNullValue()));
	}

	@Test
	void testGettingMissingChildreturnsNull() throws Exception {
		Group group = GroupImpl.createGroup(fc, sb, 800, "datasets_group", rootGroup);
		Node child = group.getChild("made_up_missing_child_name");
		assertThat(child, is(nullValue()));
	}
}
