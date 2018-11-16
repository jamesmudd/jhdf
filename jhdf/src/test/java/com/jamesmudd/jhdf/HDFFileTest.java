package com.jamesmudd.jhdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;

import com.jamesmudd.jhdf.exceptions.HdfException;

import static org.hamcrest.CoreMatchers.*;


public class HDFFileTest {

	private String testFileUrl;
	private String nonHdfFile;
	
	@Before
	public void setup() throws FileNotFoundException {
		testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		nonHdfFile = this.getClass().getResource("make_test_file.py").getFile();
	}

	@Test
	public void testOpeningValidFile() throws IOException {
		File file = new File(testFileUrl);
		try (HDFFile hdfFile = new HDFFile(new File(testFileUrl))) {
			assertThat(hdfFile.getUserHeaderSize(), is(equalTo(0L)));
			assertThat(hdfFile.length(), is(equalTo(file.length())));

			// TODO Add a test file with an actual header and read it.
			hdfFile.getUserHeader();
		}
	}
	
	@Test(expected=HdfException.class)
	public void testOpeningInvalidFile() throws IOException {
		try (HDFFile hdfFile = new HDFFile(new File(nonHdfFile))){}		 
	}
	
}
