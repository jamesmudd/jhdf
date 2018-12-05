package com.jamesmudd.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class HdfFileTest {

	private String testFileUrl;
	private String nonHdfFile;

	@Before
	public void setup() throws FileNotFoundException {
		testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		nonHdfFile = this.getClass().getResource("make_test_files.py").getFile();
	}

	@Test
	public void testOpeningValidFile() throws IOException {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getUserHeaderSize(), is(equalTo(0L)));
			assertThat(hdfFile.length(), is(equalTo(file.length())));

			// TODO Add a test file with an actual header and read it.
			hdfFile.getUserHeader();
		}
	}

	@Test(expected = HdfException.class)
	public void testOpeningInvalidFile() throws IOException {
		HdfFile hdfFile = new HdfFile(new File(nonHdfFile)); // Should throw
		hdfFile.close(); // Will not be executed
	}

	@Test
	public void testRootGroup() throws Exception {
		try (HdfFile hdfFile = new HdfFile(new File(testFileUrl))) {
			assertThat(hdfFile.getRootGroup().getName(), is(equalTo("/")));
		}
	}

}
