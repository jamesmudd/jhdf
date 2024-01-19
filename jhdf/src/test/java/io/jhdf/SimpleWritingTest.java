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

import io.jhdf.api.WritableGroup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleWritingTest {

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
		hdfFile.getChildren();

		System.out.println(hdfFile.getFile().getAbsoluteFile().getPath());

		// Cleanup
//		Files.delete(tempFile);
	}
}
