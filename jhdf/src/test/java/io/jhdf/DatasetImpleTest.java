package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.api.Dataset;

public class DatasetImpleTest {

	private static final String HDF5_TEST_FILE_NAME = "test_file.hdf5";
	private static final String TEST_FILE_URL = DatasetImpleTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();

	private HdfFile hdfFile;

	@BeforeEach
	public void before() {
		hdfFile = new HdfFile(new File(TEST_FILE_URL));
	}

	@Test
	void testGettingDatasetBuffer() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/float/float32");
		// We know this is a float dataset so get the float buffer
		// TODO the getDataBuffer method should automatically return a buffer of the
		// right endiness and type if possible
		FloatBuffer buffer = dataset.getDataBuffer().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
		assertThat(buffer, is(notNullValue()));
		// Check the size of the buffer is correct, i.e the number of points in this
		// data set
		assertThat(buffer.limit(), is(equalTo(21)));
		float[] data = new float[21];
		// read the data into the array
		buffer.get(data);
		// check the data is correct
		assertArrayEquals(new float[] { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
				data, 0.0000001f);

	}

}
