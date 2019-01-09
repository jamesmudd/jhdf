package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;

public class HdfFileTest {

	private static final String HDF5_TEST_FILE_NAME = "test_file.hdf5";
	private static final String HDF5_TEST_FILE_TWO_NAME = "test_file2.hdf5";
	private static final String NON_HDF5_TEST_FILE_NAME = "make_test_files.py";
	private String testFileUrl;
	private String nonHdfFile;
	private String testFile2Url;

	@BeforeEach
	public void setup() throws FileNotFoundException {
		testFileUrl = this.getClass().getResource(HDF5_TEST_FILE_NAME).getFile();
		testFile2Url = this.getClass().getResource(HDF5_TEST_FILE_TWO_NAME).getFile();
		nonHdfFile = this.getClass().getResource(NON_HDF5_TEST_FILE_NAME).getFile();
	}

	@Test
	public void testOpeningValidFile() throws IOException {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getUserHeaderSize(), is(equalTo(0L)));
			assertThat(hdfFile.length(), is(equalTo(file.length())));
			assertThat(hdfFile.getAddress(), is(equalTo(96L)));

			// TODO Add a test file with an actual header and read it.
			hdfFile.getUserHeader();
		}
	}

	@Test
	public void testOpeningInvalidFile() throws IOException {
		HdfException ex = assertThrows(HdfException.class, () -> new HdfFile(new File(nonHdfFile)));
		assertThat(ex.getMessage(), is(equalTo("No valid HDF5 signature found")));
	}

	@Test
	public void testOpeningMissingFile() throws IOException {
		HdfException ex = assertThrows(HdfException.class,
				() -> new HdfFile(new File("madeUpFileNameThatDoesntExist.hello")));
		assertThat(ex.getMessage(), is(equalTo("Failed to open file. Is it a HDF5 file?")));
		assertThat(ex.getCause(), is(instanceOf(IOException.class)));
	}

	@Test
	public void testRootGroup() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getName(), is(equalTo(HDF5_TEST_FILE_NAME)));
			assertThat(hdfFile.getType(), is(equalTo(NodeType.FILE)));
		}
	}

	@Test
	public void testNodesUnderTheRootGroupHaveTheRightPath() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			Group firstGroup = (Group) hdfFile.getChildren().values().iterator().next();
			String firstGroupName = firstGroup.getName();
			assertThat(firstGroup.getPath(), is(equalTo("/" + firstGroupName + "/")));
			assertThat(firstGroup.getParent(), is(sameInstance(hdfFile)));

			// Check the second level objects also have the right path as the root group is
			// a special case
			Node secondLevelGroup = firstGroup.getChildren().values().iterator().next();
			String secondLevelGroupName = secondLevelGroup.getName();
			assertThat(secondLevelGroup.getPath(),
					is(equalTo("/" + firstGroupName + "/" + secondLevelGroupName + "/")));
			assertThat(secondLevelGroup.getParent(), is(sameInstance(firstGroup)));
		}
	}

	@Test
	public void testIteratingFile() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			final Iterator<Node> iterator = hdfFile.iterator();
			assertThat(iterator.hasNext(), is(true));
		}
	}

	@Test
	void testGettingTheFileBackFromAGroup() throws Exception {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			for (Node node : hdfFile) {
				assertThat(node.getFile(), is(Matchers.sameInstance(file)));
			}
		}
	}

	@Test // This is to ensure no exceptions are thrown when inspecting the whole file
	void recurseThroughTheFileCallingBasicMethodsOnAllNodes() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			recurseGroup(hdfFile);
		}

		try (HdfFile hdfFile = new HdfFile(new File(testFile2Url))) {
			recurseGroup(hdfFile);
		}

	}

	private void recurseGroup(Group group) {
		for (Node node : group) {
			assertThat(node.getName(), not(isEmptyString()));
			assertThat(node.getAddress(), is(greaterThan(1L)));
			assertThat(node.getParent(), is(notNullValue()));
			if (node instanceof Dataset) {
				assertThat(node.isGroup(), is(false));
				assertThat(node.getType(), is(NodeType.DATASET));
			}
			if (node instanceof Group) {
				assertThat(node.isGroup(), is(true));
				assertThat(node.getType(), is(NodeType.GROUP));
				recurseGroup((Group) node);
			}
		}
	}

	@Test
	void testGettingChildByName() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getChild("datasets_group"), is(notNullValue()));
			assertThat(hdfFile.getChild("non_existent_child"), is(nullValue()));
		}
	}

	@Test
	void testHdfFileHasNoParent() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getParent(), is(nullValue()));
		}
	}

	@Test
	void testHdfFileIsGroup() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.isGroup(), is(true));
		}
	}

	@Test
	void testFormatOfToString() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.toString(), is(equalTo("HdfFile [file=test_file.hdf5]")));
		}
	}

	@Test
	void testGettingHdfFileAttributes() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getAttributes(), is(notNullValue()));
		}

	}

}
