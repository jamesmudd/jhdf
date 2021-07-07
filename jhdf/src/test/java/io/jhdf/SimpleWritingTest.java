package io.jhdf;

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
		writableHdfFile.close();

		Files.copy(tempFile, Paths.get("test.hdf5"));
		// Now read it back
		HdfFile hdfFile = new HdfFile(tempFile);
	}
}
