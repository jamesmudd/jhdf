package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.jhdf.api.Dataset;

public class DatasetImplTest {

	private static final String HDF5_TEST_FILE_NAME = "test_file.hdf5";
	private static final String TEST_FILE_URL = DatasetImplTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();

	// These are the "correct" contents of the datasets i.e need to match what was
	// written by h5py
	private static final byte[] REFERENCE_BYTE_DATA = new byte[] { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3,
			4, 5, 6, 7, 8, 9, 10 };
	private static final short[] REFERENCE_SHORT_DATA = new short[] { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
			3, 4, 5, 6, 7, 8, 9, 10 };
	private static final int[] REFERENCE_INT_DATA = new int[] { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4,
			5, 6, 7, 8, 9, 10 };
	private static final double[] REFERENCE_DOUBLE_DATA = new double[] { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1,
			2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private static final float[] REFERENCE_FLOAT_DATA = new float[] { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
			3, 4, 5, 6, 7, 8, 9, 10 };

	// Tolerance to use when comparing float values
	private static final float FLOAT_DELTA = 0.000000001f;
	private static final double DOUBLE_DELTA = 0.000000001;

	private static HdfFile hdfFile;

	@BeforeAll
	static void beforeAll() {
		hdfFile = new HdfFile(new File(TEST_FILE_URL));
	}

	@AfterAll
	static void afterAll() throws IOException {
		hdfFile.close();
	}

	@Test
	void testByteDatasetBuffer() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int8");
		// We know this is a float dataset so get the float buffer
		ByteBuffer buffer = dataset.getDataBuffer();
		assertThat(buffer, is(notNullValue()));
		// Check the size of the buffer is correct, i.e the number of points in this
		// data set
		assertThat(buffer.limit(), is(equalTo(21)));
		byte[] data = new byte[21];
		// read the data into the array
		buffer.get(data);
		// check the data is correct
		assertArrayEquals(REFERENCE_BYTE_DATA, data);
	}

	@Test
	void testShortDatasetBuffer() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int16");
		// We know this is a short dataset so get the short buffer
		ShortBuffer buffer = dataset.getDataBuffer().asShortBuffer();
		assertThat(buffer, is(notNullValue()));
		// Check the size of the buffer is correct, i.e the number of points in this
		// data set
		assertThat(buffer.limit(), is(equalTo(21)));
		short[] data = new short[21];
		// read the data into the array
		buffer.get(data);
		// check the data is correct
		assertArrayEquals(REFERENCE_SHORT_DATA, data);
	}

	@Test
	void testIntDatasetBuffer() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int32");
		// We know this is a int dataset so get the int buffer
		IntBuffer buffer = dataset.getDataBuffer().asIntBuffer();
		assertThat(buffer, is(notNullValue()));
		// Check the size of the buffer is correct, i.e the number of points in this
		// data set
		assertThat(buffer.limit(), is(equalTo(21)));
		int[] data = new int[21];
		// read the data into the array
		buffer.get(data);
		// check the data is correct
		assertArrayEquals(REFERENCE_INT_DATA, data);
	}

	@Test
	void testFloatDatasetBuffer() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/float/float32");
		// We know this is a float dataset so get the float buffer
		FloatBuffer buffer = dataset.getDataBuffer().asFloatBuffer();
		assertThat(buffer, is(notNullValue()));
		// Check the size of the buffer is correct, i.e the number of points in this
		// data set
		assertThat(buffer.limit(), is(equalTo(21)));
		float[] data = new float[21];
		// read the data into the array
		buffer.get(data);
		// check the data is correct
		assertArrayEquals(REFERENCE_FLOAT_DATA, data, FLOAT_DELTA);
	}

	@Test
	void testDoubleDatasetBuffer() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/float/float64");
		// We know this is a double dataset so get the double buffer
		DoubleBuffer buffer = dataset.getDataBuffer().asDoubleBuffer();
		assertThat(buffer, is(notNullValue()));
		// Check the size of the buffer is correct, i.e the number of points in this
		// data set
		assertThat(buffer.limit(), is(equalTo(21)));
		double[] data = new double[21];
		// read the data into the array
		buffer.get(data);
		// check the data is correct
		assertArrayEquals(REFERENCE_DOUBLE_DATA, data, DOUBLE_DELTA);
	}

	@Test
	void testByteDatasetSize() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int8");
		assertThat(dataset.getSize(), is(equalTo(21L)));
	}

	@Test
	void testByteDatasetDiskSize() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int8");
		assertThat(dataset.getDiskSize(), is(equalTo(21L)));
	}

	@Test
	void testShortDatasetSize() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int16");
		assertThat(dataset.getSize(), is(equalTo(21L)));
	}

	@Test
	void testShortDatasetDiskSize() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int16");
		assertThat(dataset.getDiskSize(), is(equalTo(42L)));
	}

	@Test
	void testIntDatasetSize() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int32");
		assertThat(dataset.getSize(), is(equalTo(21L)));
	}

	@Test
	void testIntDatasetDiskSize() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int32");
		assertThat(dataset.getDiskSize(), is(equalTo(84L)));
	}

	@Test
	void testIntDatasetDimensions() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int32");
		assertArrayEquals(dataset.getDimensions(), new long[] { 21 });
	}

	@Test
	void testIntDatasetMaxSizes() throws Exception {
		Dataset dataset = (Dataset) hdfFile.getByPath("/datasets_group/int/int32");
		assertArrayEquals(dataset.getMaxSize().get(), new long[] { 21 });
	}

}
