/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import io.jhdf.api.WritableGroup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleWritingTest {

	@Test
	void writeSimpleFile() throws Exception {
		Path tempFile = Files.createTempFile(null, ".hdf5");
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
		HdfFile hdfFile = new HdfFile(tempFile);
		Map<String, Node> children = hdfFile.getChildren();

		assertThat(children).containsKeys("testGroup", "testGroup2", "testGroup3");

		// Cleanup
		Files.delete(tempFile);
	}	@Test

	void writeSimpleFileWithDataset() throws Exception {
		Path tempFile = Files.createTempFile(null, ".hdf5");
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

		writableHdfFile.close();

		// Now read it back
		HdfFile hdfFile = new HdfFile(tempFile);
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

		// Cleanup
		Files.delete(tempFile);
	}
}
