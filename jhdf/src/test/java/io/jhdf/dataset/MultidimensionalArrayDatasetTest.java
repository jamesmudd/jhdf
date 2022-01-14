/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import static io.jhdf.TestUtils.loadTestHdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MultidimensionalArrayDatasetTest {

	private static final String HDF5_TEST_FILE_NAME = "test_multidimensional_array.hdf5";

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
	void testGetData1() {

            Node node = hdfFile.getByPath("GROUP1/GROUP2/DATASET1");
            NodeType type = node.getType();

            assert (type == NodeType.DATASET);
            Dataset dataset = (Dataset)node;

            Object data = dataset.getData();
            assertTrue(data instanceof Map);

            Class<?> javaType = dataset.getJavaType();
            assertEquals(Map.class,javaType);

            Map<String,Object> map = (Map<String,Object>)data;

            String memberName;
            Object[][] member;

            memberName = "myReferencePoint";
            assertTrue(map.containsKey(memberName));
            member = (Object[][])map.get(memberName);

            assertEquals(5,member.length);

            assertArrayEquals(new double[]{ 0., 0., 0.},(double[])member[0][0],1.0E-9);
            assertArrayEquals(new double[]{ 0., 0., 0.},(double[])member[1][0],1.0E-9);

            memberName = "myAxisVectors";
            assertTrue(map.containsKey(memberName));
            member = (Object[][])map.get(memberName);

            assertEquals(5,member.length);

            assertArrayEquals(new double[]{ 1., 0., 0., 0., 1., 0., 0., 0., 1.},(double[])member[0][0],1.0E-9);
            assertArrayEquals(new double[]{ 0., 1., 0., 1., 0., 0., 0., 0.,-1.},(double[])member[2][0],1.0E-9);
	}

	@Test
	void testGetData2() {

            Node node = hdfFile.getByPath("GROUP1/GROUP2/DATASET2");
            NodeType type = node.getType();

            assert (type == NodeType.DATASET);
            Dataset dataset = (Dataset)node;

            Map<String,Object> data = (Map<String,Object>)dataset.getData();

            String memberName;
            Object[][] member;

            memberName = "myUnitDimension";
            assertTrue(data.containsKey(memberName));
            member = (Object[][])data.get(memberName);

            assertEquals(8,member.length);

            assertArrayEquals(new int[]{ 1, 0, 0, 0, 0, 0, 0},(int[])member[0][0]);
            assertArrayEquals(new int[]{ 0, 1, 0, 0, 0, 0, 0},(int[])member[1][0]);
            assertArrayEquals(new int[]{ 0, 0, 1, 0, 0, 0, 0},(int[])member[2][0]);
            assertArrayEquals(new int[]{ 0, 0, 0, 1, 0, 0, 0},(int[])member[3][0]);
            assertArrayEquals(new int[]{ 0, 0, 0, 0, 1, 0, 0},(int[])member[4][0]);
            assertArrayEquals(new int[]{ 0, 0, 0, 0, 0, 1, 0},(int[])member[5][0]);
            assertArrayEquals(new int[]{ 0, 0, 0, 0, 0, 0, 1},(int[])member[6][0]);
            assertArrayEquals(new int[]{-1, 1,-2, 0, 0, 0, 0},(int[])member[7][0]);
	}
}
