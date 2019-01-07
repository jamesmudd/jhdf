package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.exceptions.HdfException;

public class HdfFileTest {

	private static final String NON_HDF5_TEST_FILE_NAME = "make_test_files.py";
	private static final String HDF5_TEST_FILE_NAME = "test_file.hdf5";
	private String testFileUrl;
	private String nonHdfFile;

	@BeforeEach
	public void setup() throws FileNotFoundException {
		testFileUrl = this.getClass().getResource(HDF5_TEST_FILE_NAME).getFile();
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
		assertThrows(HdfException.class, () -> new HdfFile(new File(nonHdfFile)));
	}

	@Test
	public void testRootGroup() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getName(), is(equalTo(HDF5_TEST_FILE_NAME)));
			assertThat(hdfFile.getType(), is(equalTo("HDF5 file")));
		}
	}

	@Test
	public void testNodesUnderTheRootGroupHaveTheRightPath() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			Node firstGroup = hdfFile.getChildren().values().iterator().next();
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

}
