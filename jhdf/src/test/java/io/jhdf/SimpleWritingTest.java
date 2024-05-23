/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import io.jhdf.api.WritableGroup;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
class SimpleWritingTest {
	private static Path tempFile;

	@BeforeAll
	static void beforeAll() throws IOException {
		tempFile = Files.createTempFile(null, ".hdf5");
	}

	@Test
	@Order(1)
	void writeSimpleFile() {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);
		WritableGroup testGroup = writableHdfFile.putGroup("testGroup");
		testGroup.putGroup("nested1");
		WritableGroup testGroup2 = writableHdfFile.putGroup("testGroup2");
		WritableGroup hello = testGroup2.putGroup("hello");
		WritableGroup hello2 = hello.putGroup("hello2");
		hello2.putGroup("hello3");
		WritableGroup testGroup3 = writableHdfFile.putGroup("testGroup3");
		testGroup3.putGroup("nested3");
		testGroup3.putGroup("nested33");
		testGroup3.putGroup("nested333");

		writableHdfFile.close();

		// Now read it back
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Node> children = hdfFile.getChildren();
			assertThat(children).containsKeys("testGroup", "testGroup2", "testGroup3");
		}
	}

	@Test
	@Order(2) // first test writes the file
	@EnabledIfH5DumpAvailable
	void readSimpleFileWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}

	@Test
	@Order(3)
	void writeSimpleFileWithDatasets() {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		WritableGroup intGroup = writableHdfFile.putGroup("intGroup");
		int[] intData1 = new int[]{-5, -4, -3, -2, -1, 0, 1,2,3,4,5 };
		intGroup.putDataset("intData1", intData1);

		int[] intData2 = new int[]{-500, -412, -399, -211, -54, 7, 23, 222, 34245, 412, 5656575 };
		intGroup.putDataset("intData2", intData2);

		int[][] intData3 = new int[][]{
			{-500, -412, -399, -211},
			{-54, 7, 23, -34245},
			{412, 5656575, 23, 9909}};
		intGroup.putDataset("intData3", intData3);

		WritableGroup doubleGroup = writableHdfFile.putGroup("doubleGroup");
		double[] doubleData1 = new double[]{-3300000.0,44000.0,3.0,10.0,20.0};
		doubleGroup.putDataset("doubleData1", doubleData1);

		WritableGroup byteGroup = writableHdfFile.putGroup("byteGroup");
		byte [] byteData1 = new byte[]{-10, -5, 0, 5, 10, 20};
		byteGroup.putDataset("byteData1", byteData1);

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Node> children = hdfFile.getChildren();
			assertThat(children).containsKeys("intGroup");

			Dataset intData1Dataset = hdfFile.getDatasetByPath("/intGroup/intData1");
			Object intData1ReadBack = intData1Dataset.getData();
			assertThat(intData1ReadBack).isEqualTo(intData1);

			Dataset intData2Dataset = hdfFile.getDatasetByPath("intGroup/intData2");
			Object intData2ReadBack = intData2Dataset.getData();
			assertThat(intData2ReadBack).isEqualTo(intData2);

			Dataset intData3Dataset = hdfFile.getDatasetByPath("intGroup/intData3");
			Object intData3Data = intData3Dataset.getData();
			assertThat(intData3Data).isEqualTo(intData3);

			Dataset byteData1Dataset = hdfFile.getDatasetByPath("byteGroup/byteData1");
			Object byteData1Data = byteData1Dataset.getData();
			assertThat(byteData1Data).isEqualTo(byteData1);

			Dataset doubleData1Dataset = hdfFile.getDatasetByPath("doubleGroup/doubleData1");
			Object doubleData1ReadBack = doubleData1Dataset.getData();
			assertThat(doubleData1ReadBack).isEqualTo(doubleData1);
		}
	}

	@Test
	@Order(4) // 3rd test writes the file
	@EnabledIfH5DumpAvailable
	void readSimpleFileWithDatasetsWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}

	@Test
	@Order(5)
	void writeAttributes() throws Exception {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		WritableGroup intGroup = writableHdfFile.putGroup("intGroup");
		int[] intData1 = new int[]{-5, -4, -3, -2, -1, 0, 1,2,3,4,5 };
		intGroup.putDataset("intData1", intData1);

		writableHdfFile.putAttribute("rootAttribute", new int[] {1,2,3});

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Attribute> attributes = hdfFile.getAttributes();
			assertThat(attributes).containsKeys("rootAttribute");
		}
	}
}
