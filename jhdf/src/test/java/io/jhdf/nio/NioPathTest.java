/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.nio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.datatype.DataType;

/**
 * This test ensures that jHDF supports loading HDF5 files referenced by instances of {@link java.nio.file.Path}
 * that do not reside in the default file system. To do so, the test
 * <ol>
 *     <li>
 *         copies all test files into a temporary {@code ZipFileSystem} and
 *     </li>
 *     <li>
 *         evaluates that loading the local file and loading the non-local copy yields the same HDF5 structure representation.
 *     </li>
 * </ol>
 */
class NioPathTest
{
	private static final String HDF5_TEST_FILE_DIRECTORY_PATH	= "/hdf5";
	private static final String ZIP_FILE_NAME					= "nio_test.zip";
	private static       Path   TEMP_DIR;
	private static       Path   LOCAL_ROOT_DIRECTORY;
	private static       URI    NON_LOCAL_ROOT_DIRECTORY_URI;

	@BeforeAll
	static void setup() throws IOException {
		LOCAL_ROOT_DIRECTORY = getPathToResource(HDF5_TEST_FILE_DIRECTORY_PATH);
		TEMP_DIR = Files.createTempDirectory("jHDF_NIO_test");
		Path zipFile = TEMP_DIR.resolve(ZIP_FILE_NAME);
		try (FileSystem zipFileSystem = createZipFileSystem(zipFile)) {
			Path nonLocalRootDirectory = zipFileSystem.getPath("/");
			NON_LOCAL_ROOT_DIRECTORY_URI = nonLocalRootDirectory.toUri();
			copyFiles(LOCAL_ROOT_DIRECTORY, nonLocalRootDirectory);
		}
	}

	@AfterAll
	static void shutdown() throws IOException {
		if (TEMP_DIR != null && Files.isDirectory(TEMP_DIR)) {
			delete(TEMP_DIR);
		}
	}

	@ParameterizedTest
	@MethodSource("getTestFileNames")
	void testNonDefaultFileSystemAccess(String testFileName) throws IOException {
		Path localTestFile = LOCAL_ROOT_DIRECTORY.resolve(testFileName);
		try (FileSystem ignored = openZipFileSystem(NON_LOCAL_ROOT_DIRECTORY_URI)) {
			Path nonLocalRootDirectory = Paths.get(NON_LOCAL_ROOT_DIRECTORY_URI);
			Path nonLocalTestFile = nonLocalRootDirectory.resolve(testFileName);
			compareStructure(localTestFile, nonLocalTestFile);
		}
	}

	private void compareStructure(Path file1, Path file2) {
		HdfFile hdfFile1 = new HdfFile(file1);
		HdfFile hdfFile2 = new HdfFile(file2);
		compareNodes(hdfFile1, hdfFile2);
	}

	private void compareNodes(Node node1, Node node2) {
		assertThat("Deviating node names", node1.getName(), is(node2.getName()));
		String errorSuffix = " of nodes '" + node1.getName() + "'";

		assertThat("Deviating paths" + errorSuffix, node1.getPath(), is(node2.getPath()));
		assertThat("Deviating isLink flags" + errorSuffix, node1.isLink(), is(node2.isLink()));

		boolean brokenLink = node1.isLink() && ((Link) node1).isBrokenLink();

		if (!brokenLink) {
			// the following checks lead to exceptions for broken links
			assertThat("Deviating types" + errorSuffix, node1.getType(), is(node2.getType()));
			assertThat("Deviating isGroup flags" + errorSuffix, node1.isGroup(), is(node2.isGroup()));
			assertThat("Deviating addresses" + errorSuffix, node1.getAddress(), is(node2.getAddress()));
			assertThat("Deviating isAttributeCreationOrderTracked flags" + errorSuffix, node1.isAttributeCreationOrderTracked(), is(node2.isAttributeCreationOrderTracked()));

			Map<String, Attribute> attributes1 = node1.getAttributes();
			Map<String, Attribute> attributes2 = node2.getAttributes();
			assertThat("Deviating number of attributes" + errorSuffix, attributes1.size(), is(attributes2.size()));
			for (Entry<String, Attribute> attributeEntry1 : attributes1.entrySet()) {
				String attributeName = attributeEntry1.getKey();
				Attribute attribute1 = attributeEntry1.getValue();
				Attribute attribute2 = attributes2.get(attributeName);
				assertThat("Missing attribute '" + attributeName + "' in second node '" + node2.getName() + "'", attributes2, is(notNullValue()));
				compareAttributes(attribute1, attribute2);
			}
		}

		if (node1 instanceof Link) {
			assertThat("Node '" + node2.getName() + "' is not a link", node2 instanceof Link);
			compareLinks((Link) node1, (Link) node2);
		} else if (node1 instanceof Group) {
			assertThat("Node '" + node2.getName() + "' is not a group", node2 instanceof Group);
			compareGroups((Group) node1, (Group) node2);
		} else if (node1 instanceof Dataset) {
			assertThat("Node '" + node2.getName() + "' is not a dataset", node2 instanceof Dataset);
			compareDatasets((Dataset) node1, (Dataset) node2);
		}
	}

	private void compareAttributes(Attribute attribute1, Attribute attribute2) {
		assertThat("Deviating attribute names", attribute1.getName(), is(attribute2.getName()));
		String errorSuffix = " of attributes '" + attribute1.getName() + "'";

		assertThat("Deviating sizes" + errorSuffix, attribute1.getSize(), is(attribute2.getSize()));
		assertThat("Deviating sizes in bytes" + errorSuffix, attribute1.getSizeInBytes(), is(attribute2.getSizeInBytes()));
		assertThat("Deviating dimensions" + errorSuffix, attribute1.getDimensions(), is(attribute2.getDimensions()));
		assertThat("Deviating Java types" + errorSuffix, attribute1.getJavaType(), is(attribute2.getJavaType()));
		assertThat("Deviating isScalar flags" + errorSuffix, attribute1.isScalar(), is(attribute2.isScalar()));
		assertThat("Deviating isScalar flags" + errorSuffix, attribute1.isEmpty(), is(attribute2.isEmpty()));
	}

	private void compareLinks(Link link1, Link link2) {
		String errorSuffix = " of links '" + link1.getName() + "'";

		assertThat("Deviating target paths" + errorSuffix, link1.getTargetPath(), is(link2.getTargetPath()));
		assertThat("Deviating isBrokenLink flags" + errorSuffix, link1.isBrokenLink(), is(link2.isBrokenLink()));
	}

	private void compareGroups(Group group1, Group group2) {
		String errorSuffix = " of groups '" + group1.getName() + "'";

		assertThat("Deviating isLinkCreationOrderTracked flags" + errorSuffix, group1.isLinkCreationOrderTracked(), is(group2.isLinkCreationOrderTracked()));

		Map<String, Node> children1 = group1.getChildren();
		Map<String, Node> children2 = group2.getChildren();
		assertThat("Deviating number of children" + errorSuffix, children1.size(), is(children2.size()));

		for (Entry<String, Node> childEntry1 : children1.entrySet()) {
			String childName = childEntry1.getKey();
			Node child1 = childEntry1.getValue();
			Node child2 = children2.get(childName);
			assertThat("Missing child '" + childName + "' in second group '" + group2.getName() + "'", child2, is(notNullValue()));
			compareNodes(child1, child2);
		}
	}

	private void compareDatasets(Dataset dataset1, Dataset dataset2) {
		String errorSuffix = " of datasets '" + dataset1.getName() + "'";

		assertThat("Deviating sizes" + errorSuffix, dataset1.getSize(), is(dataset2.getSize()));
		assertThat("Deviating sizes in bytes" + errorSuffix, dataset1.getSizeInBytes(), is(dataset2.getSizeInBytes()));
		assertThat("Deviating storage sizes in bytes" + errorSuffix, dataset1.getStorageInBytes(), is(dataset2.getStorageInBytes()));
		assertThat("Deviating dimensions" + errorSuffix, dataset1.getDimensions(), is(dataset2.getDimensions()));
		assertThat("Deviating isScalar flags" + errorSuffix, dataset1.isScalar(), is(dataset2.isScalar()));
		assertThat("Deviating isScalar flags" + errorSuffix, dataset1.isEmpty(), is(dataset2.isEmpty()));
		assertThat("Deviating isCompound flags" + errorSuffix, dataset1.isCompound(), is(dataset2.isCompound()));
		assertThat("Deviating isVariableLength flags" + errorSuffix, dataset1.isVariableLength(), is(dataset2.isVariableLength()));
		assertThat("Deviating max sizes" + errorSuffix, dataset1.getMaxSize(), is(dataset2.getMaxSize()));
		assertThat("Deviating data layouts" + errorSuffix, dataset1.getDataLayout(), is(dataset2.getDataLayout()));
		assertThat("Deviating Java type" + errorSuffix, dataset1.getJavaType(), is(dataset2.getJavaType()));

		Object fillValue1 = null;
		boolean fillValueExists;
		try {
			fillValue1 = dataset1.getFillValue();
			fillValueExists = true;
		} catch (HdfException e) {
			fillValueExists = false;
		}
		if (fillValueExists) {
			assertThat("Deviating fill values" + errorSuffix, fillValue1, is(dataset2.getFillValue()));
		}

		DataType dataType1 = dataset1.getDataType();
		DataType dataType2 = dataset2.getDataType();
		compareDataTypes(dataType1, dataType2, errorSuffix);
	}

	private void compareDataTypes(DataType dataType1, DataType dataType2, String errorSuffix) {
		assertThat("Deviating data type versions" + errorSuffix, dataType1.getVersion(), is(dataType2.getVersion()));
		assertThat("Deviating data type data classes" + errorSuffix, dataType1.getDataClass(), is(dataType2.getDataClass()));
		assertThat("Deviating data type sizes" + errorSuffix, dataType1.getSize(), is(dataType2.getSize()));
		assertThat("Deviating data type Java classes" + errorSuffix, dataType1.getJavaType(), is(dataType2.getJavaType()));
	}

	static List<String> getTestFileNames() throws IOException {
		List<String> testFileNames = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(LOCAL_ROOT_DIRECTORY)) {
			for (Path sourceFile : stream) {
				if (Files.isRegularFile(sourceFile)) {
					testFileNames.add(sourceFile.getFileName().toString());
				}
			}
		}
		return testFileNames;
	}

	private static void delete(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
				for (Path value : stream) {
					delete(value);
				}
			}
		}
		Files.delete(path);
	}

	private static void copyFiles(Path sourceDirectory, Path targetDirectory) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory)) {
			for (Path sourceFile : stream) {
				if (Files.isRegularFile(sourceFile)) {
					Path targetFile = targetDirectory.resolve(sourceFile.getFileName().toString());
					Files.copy(sourceFile, targetFile);
				}
			}
		}
	}

	private static Path getPathToResource(String relativePath) {
		URL testFileDirectoryUrl = NioPathTest.class.getResource(relativePath);
		if (testFileDirectoryUrl == null) {
			throw new IllegalStateException("No resource URL available for relative path '" + relativePath + "'");
		}
		try {
			return Paths.get(testFileDirectoryUrl.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Invalid resource URL '" + testFileDirectoryUrl + "'");
		}
	}

	private static FileSystem createZipFileSystem(Path zipFile) throws IOException {
		URI zipFileUri = zipFile.toUri();
		URI zipUri = URI.create("jar:" + zipFileUri);
		return openZipFileSystem(zipUri);
	}

	private static FileSystem openZipFileSystem(URI uri) throws IOException {
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		return FileSystems.newFileSystem(uri, env);
	}
}
