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
import io.jhdf.api.Dataset;
import io.jhdf.object.datatype.ArrayDataType;
import io.jhdf.object.datatype.CompoundDataType;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.FloatingPoint;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

// https://github.com/jamesmudd/jhdf/issues/341
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
		Dataset dataset = hdfFile.getDatasetByPath("GROUP1/GROUP2/DATASET1");

		assertThat(dataset.getJavaType(), is(Map.class));

		Object data = dataset.getData();
		assertThat(data, isA(Map.class));
		Map<String, Object> map = (Map<String, Object>) data;

		String memberName;
		Object[][] member;

		memberName = "myReferencePoint";
		assertThat(map, Matchers.hasKey(memberName));
		member = (Object[][]) map.get(memberName);

		assertThat(member.length, is(5));
		assertArrayEquals(new double[]{0., 0., 0.}, (double[]) member[0][0], 1.0E-9);
		assertArrayEquals(new double[]{0., 0., 0.}, (double[]) member[1][0], 1.0E-9);

		memberName = "myAxisVectors";
		assertThat(map, Matchers.hasKey(memberName));
		member = (Object[][]) map.get(memberName);

		assertThat(member.length, is(5));
		assertArrayEquals(new double[]{1., 0., 0., 0., 1., 0., 0., 0., 1.}, (double[]) member[0][0], 1.0E-9);
		assertArrayEquals(new double[]{0., 1., 0., 1., 0., 0., 0., 0., -1.}, (double[]) member[2][0], 1.0E-9);
	}

	@Test
	void testGetData2() {
		Dataset dataset = hdfFile.getDatasetByPath("GROUP1/GROUP2/DATASET2");
		Map<String, Object> data = (Map<String, Object>) dataset.getData();

		String memberName;
		Object[][] member;

		memberName = "myUnitDimension";
		assertThat(data, Matchers.hasKey(memberName));
		member = (Object[][]) data.get(memberName);

		assertThat(member.length, is(8));

		assertArrayEquals(new int[]{1, 0, 0, 0, 0, 0, 0}, (int[]) member[0][0]);
		assertArrayEquals(new int[]{0, 1, 0, 0, 0, 0, 0}, (int[]) member[1][0]);
		assertArrayEquals(new int[]{0, 0, 1, 0, 0, 0, 0}, (int[]) member[2][0]);
		assertArrayEquals(new int[]{0, 0, 0, 1, 0, 0, 0}, (int[]) member[3][0]);
		assertArrayEquals(new int[]{0, 0, 0, 0, 1, 0, 0}, (int[]) member[4][0]);
		assertArrayEquals(new int[]{0, 0, 0, 0, 0, 1, 0}, (int[]) member[5][0]);
		assertArrayEquals(new int[]{0, 0, 0, 0, 0, 0, 1}, (int[]) member[6][0]);
		assertArrayEquals(new int[]{-1, 1, -2, 0, 0, 0, 0}, (int[]) member[7][0]);
	}

	@Test
	void getAccessingArrayDataType() {
		Dataset dataset = hdfFile.getDatasetByPath("GROUP1/GROUP2/DATASET1");
		DataType dataType = dataset.getDataType();
		assertThat(dataType, isA(CompoundDataType.class));

		CompoundDataType compoundDataType = (CompoundDataType) dataType;
		List<CompoundDataType.CompoundDataMember> members = compoundDataType.getMembers();
		assertThat(members, hasSize(4));

		CompoundDataType.CompoundDataMember myReferencePointMember = members.get(2);
		assertThat(myReferencePointMember.getName(), is("myReferencePoint"));
		assertThat(myReferencePointMember.getDimensionSize(), is(nullValue()));
		assertThat(myReferencePointMember.getOffset(), is(8));

		DataType myReferencePointMemberDataType = myReferencePointMember.getDataType();
		assertThat(myReferencePointMemberDataType, isA(ArrayDataType.class));
		ArrayDataType arrayDataType = (ArrayDataType) myReferencePointMemberDataType;
		assertThat(toObject(arrayDataType.getArrayTypeDimensions()), arrayContaining(3));
		assertThat(arrayDataType.getBaseType(), isA(FloatingPoint.class));
		assertThat(arrayDataType.getJavaType(), is(Array.newInstance(double.class, 0).getClass()));

	}
}
