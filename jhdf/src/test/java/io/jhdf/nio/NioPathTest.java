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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		assertEquals(node1.getName(), node2.getName(), "Deviating node names");
		String errorSuffix = " of nodes '" + node1.getName() + "'";

		assertEquals(node1.getPath(), node2.getPath(), "Deviating paths" + errorSuffix);
		assertEquals(node1.isLink(), node2.isLink(), "Deviating isLink flags" + errorSuffix);

		boolean brokenLink = node1.isLink() && ((Link) node1).isBrokenLink();

		if (!brokenLink) {
			// the following checks lead to exceptions for broken links
			assertEquals(node1.getType(), node2.getType(), "Deviating types" + errorSuffix);
			assertEquals(node1.isGroup(), node2.isGroup(), "Deviating isGroup flags" + errorSuffix);
			assertEquals(node1.getAddress(), node2.getAddress(), "Deviating addresses" + errorSuffix);
			assertEquals(node1.isAttributeCreationOrderTracked(), node2.isAttributeCreationOrderTracked(), "Deviating isAttributeCreationOrderTracked flags" + errorSuffix);

			Map<String, Attribute> attributes1 = node1.getAttributes();
			Map<String, Attribute> attributes2 = node2.getAttributes();
			assertEquals(attributes1.size(), attributes2.size(), "Deviating number of attributes" + errorSuffix);
			for (String attributeName : attributes1.keySet()) {
				assertTrue(attributes2.containsKey(attributeName), "Missing attribute '" + attributeName + "' in second node '" + node2.getName() + "'");
				Attribute attribute1 = attributes1.get(attributeName);
				Attribute attribute2 = attributes2.get(attributeName);
				compareAttributes(attribute1, attribute2);
			}
		}

		if (node1 instanceof Link) {
			assertTrue(node2 instanceof Link, "Node '" + node2.getName() + "' is not a link");
			compareLinks((Link) node1, (Link) node2);
		} else if (node1 instanceof Group) {
			assertTrue(node2 instanceof Group, "Node '" + node2.getName() + "' is not a group");
			compareGroups((Group) node1, (Group) node2);
		} else if (node1 instanceof Dataset) {
			assertTrue(node2 instanceof Dataset, "Node '" + node2.getName() + "' is not a dataset");
			compareDatasets((Dataset) node1, (Dataset) node2);
		}
	}

	private void compareAttributes(Attribute attribute1, Attribute attribute2) {
		assertEquals(attribute1.getName(), attribute2.getName(), "Deviating attribute names");
		String errorSuffix = " of attributes '" + attribute1.getName() + "'";

		assertEquals(attribute1.getSize(), attribute2.getSize(), "Deviating sizes" + errorSuffix);
		assertEquals(attribute1.getSizeInBytes(), attribute2.getSizeInBytes(), "Deviating sizes in bytes" + errorSuffix);
		assertArrayEquals(attribute1.getDimensions(), attribute2.getDimensions(), "Deviating dimensions" + errorSuffix);
		assertEquals(attribute1.getJavaType(), attribute2.getJavaType(), "Deviating Java types" + errorSuffix);
		assertEquals(attribute1.isScalar(), attribute2.isScalar(), "Deviating isScalar flags" + errorSuffix);
		assertEquals(attribute1.isEmpty(), attribute2.isEmpty(), "Deviating isScalar flags" + errorSuffix);
	}

	private void compareLinks(Link link1, Link link2) {
		String errorSuffix = " of links '" + link1.getName() + "'";

		assertEquals(link1.getTargetPath(), link2.getTargetPath(), "Deviating target paths" + errorSuffix);
		assertEquals(link1.isBrokenLink(), link2.isBrokenLink(), "Deviating isBrokenLink flags" + errorSuffix);
	}

	private void compareGroups(Group group1, Group group2) {
		String errorSuffix = " of groups '" + group1.getName() + "'";

		assertEquals(group1.isLinkCreationOrderTracked(), group2.isLinkCreationOrderTracked(), "Deviating isLinkCreationOrderTracked flags" + errorSuffix);

		Map<String, Node> children1 = group1.getChildren();
		Map<String, Node> children2 = group2.getChildren();
		assertEquals(children1.size(), children2.size(), "Deviating number of children" + errorSuffix);
		for (String childName : children1.keySet()) {
			assertTrue(children2.containsKey(childName), "Missing child '" + childName + "' in second group '" + group2.getName() + "'");
			Node child1 = children1.get(childName);
			Node child2 = children2.get(childName);
			compareNodes(child1, child2);
		}
	}

	private void compareDatasets(Dataset dataset1, Dataset dataset2) {
		String errorSuffix = " of datasets '" + dataset1.getName() + "'";

		assertEquals(dataset1.getSize(), dataset2.getSize(), "Deviating sizes" + errorSuffix);
		assertEquals(dataset1.getSizeInBytes(), dataset2.getSizeInBytes(), "Deviating sizes in bytes" + errorSuffix);
		assertEquals(dataset1.getStorageInBytes(), dataset2.getStorageInBytes(), "Deviating storage sizes in bytes" + errorSuffix);
		assertArrayEquals(dataset1.getDimensions(), dataset2.getDimensions(), "Deviating dimensions" + errorSuffix);
		assertEquals(dataset1.isScalar(), dataset2.isScalar(), "Deviating isScalar flags" + errorSuffix);
		assertEquals(dataset1.isEmpty(), dataset2.isEmpty(), "Deviating isScalar flags" + errorSuffix);
		assertEquals(dataset1.isCompound(), dataset2.isCompound(), "Deviating isCompound flags" + errorSuffix);
		assertEquals(dataset1.isVariableLength(), dataset2.isVariableLength(), "Deviating isVariableLength flags" + errorSuffix);
		assertArrayEquals(dataset1.getMaxSize(), dataset2.getMaxSize(), "Deviating max sizes" + errorSuffix);
		assertEquals(dataset1.getDataLayout(), dataset2.getDataLayout(), "Deviating data layouts" + errorSuffix);
		assertEquals(dataset1.getJavaType(), dataset2.getJavaType(), "Deviating Java type" + errorSuffix);

		Object fillValue1 = null;
		boolean fillValueExists;
		try {
			fillValue1 = dataset1.getFillValue();
			fillValueExists = true;
		} catch (HdfException e) {
			fillValueExists = false;
		}
		if (fillValueExists) {
			assertEquals(fillValue1, dataset2.getFillValue(), "Deviating fill values" + errorSuffix);
		}

		DataType dataType1 = dataset1.getDataType();
		DataType dataType2 = dataset2.getDataType();
		compareDataTypes(dataType1, dataType2, errorSuffix);
	}

	private void compareDataTypes(DataType dataType1, DataType dataType2, String errorSuffix) {
		assertEquals(dataType1.getVersion(), dataType2.getVersion(), "Deviating data type versions" + errorSuffix);
		assertEquals(dataType1.getDataClass(), dataType2.getDataClass(), "Deviating data type data classes" + errorSuffix);
		assertEquals(dataType1.getSize(), dataType2.getSize(), "Deviating data type sizes" + errorSuffix);
		assertEquals(dataType1.getJavaType(), dataType2.getJavaType(), "Deviating data type Java classes" + errorSuffix);
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
