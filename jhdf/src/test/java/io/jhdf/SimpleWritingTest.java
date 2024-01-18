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
		writableHdfFile.putGroup("testGroup2");
		writableHdfFile.putGroup("testGroup3");

		writableHdfFile.close();

		// Now read it back
		HdfFile hdfFile = new HdfFile(tempFile);
//		hdfFile.getChildren();

		System.out.println(hdfFile.getFile().getAbsoluteFile().getPath());

		// Cleanup
//		Files.delete(tempFile);
	}
}
