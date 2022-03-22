package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SlicingTest {

	private static HdfFile hdfFile;

	@BeforeAll
	static void beforeAll() {
		hdfFile = HdfFile.fromInputStream(SlicingTest.class.getResourceAsStream("/hdf5/test_file2.hdf5"));
	}


	@Test
	void test3DSliceIntDataset() {
		Dataset dataset = hdfFile.getDatasetByPath("/nD_Datasets/3D_int32");

		int[][][] data = (int[][][]) dataset.getData(new long[]{0, 2, 30}, new int[]{1, 1, 15});

		data = (int[][][]) dataset.getData(new long[]{0, 2, 30}, new int[]{1, 3, 15});

		data = (int[][][]) dataset.getData(new long[]{0, 0, 0}, new int[]{2, 5, 10});

	}
}
