/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.attribute;

import io.jhdf.HdfFile;
import static io.jhdf.TestUtils.loadTestHdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CompoundScalarAttributeTest {

	private static final String HDF5_TEST_FILE_NAME = "test_compound_scalar_attribute.hdf5";

	private static HdfFile hdfFile;

	@BeforeAll
	static void setup() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@AfterAll
	static void tearDown() {
		hdfFile.close();
	}

	@Test
	void testReadAttribute() {
            
            Node node = hdfFile.getByPath("GROUP");
            Attribute attribute = node.getAttribute("VERSION");
            
            Object data = attribute.getData();
            
            Map<String,int[]> dataImpl = (Map<String,int[]>)data;
            
            String key = "myMajor";
            assertTrue(dataImpl.containsKey(key));
            assertEquals(1,dataImpl.get(key)[0]);
            
            key = "myMinor";
            assertTrue(dataImpl.containsKey(key));
            assertEquals(0,dataImpl.get(key)[0]);
            
            key = "myPatch";
            assertTrue(dataImpl.containsKey(key));
            assertEquals(0,dataImpl.get(key)[0]);
	}
}
