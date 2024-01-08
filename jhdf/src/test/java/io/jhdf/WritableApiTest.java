package io.jhdf;

import io.jhdf.api.WritableGroup;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WritableApiTest {

	@Test
	void testBuildingFile() throws Exception {
		Path path = Paths.get("writing.hdf5");
		try (WritableHdfFile file = new WritableHdfFile(path)) {

			WritableGroup group1 = file.putGroup("group1");
			group1.putDataset("datasetInGroup1", new int[] {1,2,3,4,5});

			file.putDataset("datasetInRoot", new double[]{1,2,3,4,5});
		}
	}
}
