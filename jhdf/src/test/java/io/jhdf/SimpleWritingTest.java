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
		WritableGroup testGroup = writableHdfFile.putGroup("testGroup");
		int[] data = new int[]{-5, -4, -3, -2, -1, 0, 1,2,3,4,5 };
		testGroup.putDataset("myData", data);

		writableHdfFile.close();

		// Now read it back
		HdfFile hdfFile = new HdfFile(tempFile);
		Map<String, Node> children = hdfFile.getChildren();
		assertThat(children).containsKeys("testGroup");

		Dataset dataset = hdfFile.getDatasetByPath("/testGroup/myData");

		// Cleanup
		Files.delete(tempFile);
	}
}
