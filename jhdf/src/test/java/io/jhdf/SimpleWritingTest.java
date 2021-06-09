package io.jhdf;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleWritingTest {

	@Test
	void writeSimpleFile() {
		WritableHdfFile writableHdfFile = HdfFile.write(Paths.get("test.hdf5"));
		writableHdfFile.close();
	}
}
