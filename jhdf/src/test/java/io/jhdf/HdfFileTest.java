/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfInvalidPathException;
import io.jhdf.exceptions.InMemoryHdfException;
import io.jhdf.storage.HdfBackingStorage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HdfFileTest {

	private static final String HDF5_TEST_FILE_NAME = "test_file.hdf5";
	private static final String HDF5_TEST_FILE_PATH = "/hdf5/" + HDF5_TEST_FILE_NAME;
	private static final String HDF5_TEST_FILE_TWO_NAME = "test_file2.hdf5";
	private static final String HDF5_TEST_FILE_TWO_PATH = "/hdf5/" + HDF5_TEST_FILE_TWO_NAME;
	private static final String NON_HDF5_TEST_FILE_NAME = "/scripts/make_test_files.py";
	private String testFileUrl;
	private String nonHdfFile;
	private String testFile2Url;

	@BeforeEach
	void setup() {
		testFileUrl = this.getClass().getResource(HDF5_TEST_FILE_PATH).getFile();
		testFile2Url = this.getClass().getResource(HDF5_TEST_FILE_TWO_PATH).getFile();
		nonHdfFile = this.getClass().getResource(NON_HDF5_TEST_FILE_NAME).getFile();
	}

	@Test
	void testOpeningValidFile() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getUserBlockSize(), is(equalTo(0L)));
			assertThat(hdfFile.size(), is(equalTo(file.length())));
			assertThat(hdfFile.getAddress(), is(equalTo(96L)));

			hdfFile.getUserBlockBuffer();
		}
	}

	@Test
	void testOpeningFileWithLargeMaxDimensonSize() {
		String filePath = "/hdf5/100B_max_dimension_size.hdf5";

		testFileUrl = this.getClass().getResource(filePath).getFile();
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			Dataset dataset = hdfFile.getDatasetByPath("/100B-MaxSize");

			assertThat(dataset.getMaxSize().length, is(equalTo(1)));
			assertThat(dataset.getMaxSize()[0], is(equalTo(100000000000L)));
		}
	}

	@Test
	void testOpeningInvalidFile() {
		File file = new File(nonHdfFile);
		HdfException ex = assertThrows(HdfException.class, () -> new HdfFile(file));
		assertThat(ex.getMessage(), is(equalTo("No valid HDF5 signature found")));
	}

	@Test
	void testOpeningMissingFile() {
		File file = new File("madeUpFileNameThatDoesntExist.hello");
		HdfException ex = assertThrows(HdfException.class, () -> new HdfFile(file));
		assertThat(ex.getMessage(), is(startsWith("Failed to open file")));
		assertThat(ex.getCause(), is(instanceOf(IOException.class)));
	}

	@Test
	void testRootGroup() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getName(), is(equalTo(HDF5_TEST_FILE_NAME)));
			assertThat(hdfFile.getType(), is(equalTo(NodeType.FILE)));
		}
	}

	@Test
	void testNodesUnderTheRootGroupHaveTheRightPath() {
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
	void testIteratingFile() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			final Iterator<Node> iterator = hdfFile.iterator();
			assertThat(iterator.hasNext(), is(true));
		}
	}

	@Test
	void testGettingTheFileBackFromAGroup() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			for (Node node : hdfFile) {
				assertThat(node.getFile(), is(Matchers.sameInstance(file)));
			}
		}
	}

	@Test
		// This is to ensure no exceptions are thrown when inspecting the whole file
	void recurseThroughTheFileCallingBasicMethodsOnAllNodes() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			recurseGroup(hdfFile);
		}

		try (HdfFile hdfFile = new HdfFile(new File(testFile2Url))) {
			recurseGroup(hdfFile);
		}

	}

	private void recurseGroup(Group group) {
		for (Node node : group) {
			if (node instanceof Link) {
				Link link = (Link) node;
				// Check for broken links and skip
				if (((Link) node).isBrokenLink()) {
					continue;
				} else { // Resolve the link at check that
					node = link.getTarget();
				}
			}

			assertThat(node.getName(), is(not(emptyString())));
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
	void testGettingChildByName() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getChild("datasets_group"), is(notNullValue()));
			assertThat(hdfFile.getChild("non_existent_child"), is(nullValue()));
		}
	}

	@Test
	void testHdfFileHasNoParent() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getParent(), is(nullValue()));
		}
	}

	@Test
	void testHdfFileIsGroup() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.isGroup(), is(true));
		}
	}

	@Test
	void testFormatOfToString() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.toString(), is(equalTo("HdfFile [file=test_file.hdf5]")));
		}
	}

	@Test
	void testGettingHdfFileAttributes() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getAttributes(), is(notNullValue()));
		}
	}

	@Test
	void testGettingByPath() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			String path = "datasets_group/float/float32";
			Node node = hdfFile.getByPath(path);
			assertThat(node, is(notNullValue()));
			// Add leading '/' because its the file
			assertThat(node.getPath(), is(equalTo("/" + path)));
		}
	}

	@Test
	void testGettingByPathWithLeadingSlash() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			String path = "/datasets_group/float/float32";
			Node node = hdfFile.getByPath(path);
			assertThat(node, is(notNullValue()));
			assertThat(node.getPath(), is(equalTo(path)));
		}
	}

	@Test
	void testGettingByInvalidPathWithLeadingSlashThrows() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			String path = "/datasets_group/float/float32/invalid_name";
			HdfInvalidPathException e = assertThrows(HdfInvalidPathException.class, () -> hdfFile.getByPath(path));
			assertThat(e.getPath(), is(equalTo(path)));
			assertThat(e.getFile(), is(sameInstance(file)));
			assertThat(e.getMessage(), is(equalTo(
				"The path '/datasets_group/float/float32/invalid_name' could not be found in the HDF5 file '"
					+ file.getAbsolutePath() + "'")));
		}
	}

	@Test
	void testIsLinkIsFalse() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.isLink(), is(false));
		}
	}

	@Test
	void testAttributes() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.isAttributeCreationOrderTracked(), is(false));
			assertThat(hdfFile.getAttributes().isEmpty(), is(true));
			assertThat(hdfFile.getAttribute("missing"), is(nullValue()));
		}
	}

	@Test
	void testLinkCreationOrdered() {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.isLinkCreationOrderTracked(), is(false));
		}
	}

	@Test
	void testURIConstructor() throws URISyntaxException {
		URI uri = this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI();
		HdfFile hdfFile = new HdfFile(uri);
		assertThat(hdfFile.getFile(), is(notNullValue()));
		hdfFile.close();
	}

	@Test
	void testPathConstructor() throws URISyntaxException {
		Path path = Paths.get(this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI());
		HdfFile hdfFile = new HdfFile(path);
		assertThat(hdfFile.getFile(), is(notNullValue()));
		hdfFile.close();
	}

	@Test
	void testReadingFromStream() throws IOException {
		try (InputStream inputStream = this.getClass().getResource(HDF5_TEST_FILE_PATH).openStream();
			 HdfFile hdfFile = HdfFile.fromInputStream(inputStream)) {

			assertThat(hdfFile.getUserBlockSize(), is(equalTo(0L)));
			assertThat(hdfFile.getAddress(), is(equalTo(96L)));
		}
	}

	@Test
	void testReadingFromStreamThrowsWhenStreamCantBeRead() throws IOException {
		InputStream inputStream = Mockito.mock(InputStream.class);
		Mockito.when(inputStream.read(Mockito.any())).thenThrow(new IOException("Broken test stream"));
		assertThrows(HdfException.class, () -> HdfFile.fromInputStream(inputStream));
	}

	@Test
	void testLoadingInMemoryFile() throws IOException, URISyntaxException {
		Path path = Paths.get(this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI());
		ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(path));
		HdfFile hdfFile = HdfFile.fromByteBuffer(byteBuffer);
		assertThat(hdfFile, is(notNullValue()));
		assertThat(hdfFile.inMemory(), is(true));
		assertThat(hdfFile.getName(), is("In-Memory no backing file"));
		hdfFile.close();
	}

	@Test
	void testLoadingInMemoryFileFromByteArray() throws IOException, URISyntaxException {
		Path path = Paths.get(this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI());
		byte[] bytes = Files.readAllBytes(path);
		HdfFile hdfFile = HdfFile.fromBytes(bytes);
		assertThat(hdfFile, is(notNullValue()));
		assertThat(hdfFile.inMemory(), is(true));
		assertThat(hdfFile.size(), is(equalTo(Integer.toUnsignedLong(bytes.length))));
		assertThat(hdfFile.getUserBlockSize(), is(equalTo(0L)));
		assertThat(hdfFile.getName(), is("In-Memory no backing file"));
		hdfFile.close();
	}

	@Test
	void testLoadingInMemoryFileFromByteArrayTwo() throws IOException, URISyntaxException {
		Path path = Paths.get(this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI());
		byte[] bytes = Files.readAllBytes(path);
		HdfFile hdfFile = HdfFile.fromBytes(bytes);
		assertThat(hdfFile, is(notNullValue()));
		assertThat(hdfFile.inMemory(), is(true));
		assertThat(hdfFile.size(), is(equalTo(Integer.toUnsignedLong(bytes.length))));
		assertThat(hdfFile.getUserBlockSize(), is(equalTo(0L)));
		assertThat(hdfFile.getName(), is("In-Memory no backing file"));
		hdfFile.close();
	}

	@Test
	void testReadingFromEmptyByteArrayFails() {
		byte[] bytes = new byte[0];
		assertThrows(HdfException.class, () -> HdfFile.fromBytes(bytes));
	}

	@Test
	void testExternalFilesOnInMemoryThrows() throws IOException, URISyntaxException {
		Path path = Paths.get(this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI());
		byte[] bytes = Files.readAllBytes(path);
		HdfFile hdfFile = HdfFile.fromBytes(bytes);
		assertThrows(InMemoryHdfException.class, () -> hdfFile.addExternalFile(Mockito.mock(HdfFile.class)));
	}

	@Test
	void testGettingFileChannelFromInMemoryFileThrows() throws IOException, URISyntaxException {
		Path path = Paths.get(this.getClass().getResource(HDF5_TEST_FILE_PATH).toURI());
		byte[] bytes = Files.readAllBytes(path);
		HdfFile hdfFile = HdfFile.fromBytes(bytes);
		HdfBackingStorage hdfBackingStorage = hdfFile.getHdfBackingStorage();
		assertThrows(InMemoryHdfException.class, hdfBackingStorage::getFileChannel);
	}
}
