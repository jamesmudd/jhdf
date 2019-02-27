package io.jhdf.examples;

import org.junit.jupiter.api.Test;

public class ReadDatasetTest {

	@Test
	public void testOpeningV1File() {
		ReadDataset.main(new String[] { "src/test/resources/io/jhdf/test_file.hdf5", "/nD_Datasets/3D_float32" });
	}

	@Test
	public void testOpeningV2File() {
		ReadDataset.main(new String[] { "src/test/resources/io/jhdf/test_file2.hdf5", "/nD_Datasets/3D_float32" });
	}

}
