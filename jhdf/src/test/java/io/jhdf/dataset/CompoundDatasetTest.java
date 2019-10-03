/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.TestUtils;
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static io.jhdf.TestUtils.getDimensions;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CompoundDatasetTest {

    private static final String HDF5_TEST_FILE_NAME = "../compound_dataset_test.hdf5";

    private static HdfFile hdfFile;

    @BeforeAll
    static void setup() {
        String testFileUrl = ChunkedDatasetTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();
        hdfFile = new HdfFile(new File(testFileUrl));
    }

    @Test
    void test2dCompound() {
        Dataset twoDCompound = hdfFile.getDatasetByPath("/2d_compound");
        // General checks
        assertThat(twoDCompound.getDimensions(), is(equalTo(new int[]{2,2})));
        assertThat(twoDCompound.getMaxSize(), is(equalTo(new int[]{-1,-1})));
        assertThat(twoDCompound.getJavaType(), is(Map.class));
        assertThat(twoDCompound.getFillValue(), is(nullValue()));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) twoDCompound.getData();
        assertThat(compoundData.size(), is(equalTo(2)));

        // Check the int element should be a 2x2 array
        int[][] intData = (int[][]) compoundData.get("int");
        assertThat(intData, is(equalTo(new int[][]{{1,2},{3,4}})));

        // Check float element
        float[][] floatData = (float[][]) compoundData.get("float");
        assertThat(floatData, is(equalTo(new float[][]{{1,2},{3,4}})));
    }

    @Test
    void testChunkedCompound() {
        Dataset chunkedCompound = hdfFile.getDatasetByPath("/chunked_compound");
        // General checks
        assertThat(chunkedCompound.getDimensions(), is(equalTo(new int[]{4})));
        assertThat(chunkedCompound.getMaxSize(), is(equalTo(new int[]{4})));
        assertThat(chunkedCompound.getJavaType(), is(Map.class));
        assertThat(chunkedCompound.getFillValue(), is(nullValue()));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) chunkedCompound.getData();
        assertThat(compoundData.size(), is(equalTo(2)));

        // Check the name element should be a string array
        String[] nameData = (String[]) compoundData.get("name");
        assertThat(nameData, is(equalTo(new String[]{"one", "two", "three", "four"})));

        // Check vector element
        double[][] doubleData = (double[][]) compoundData.get("vector");
        assertThat(doubleData, is(equalTo(new double[][]{{1.0,2.0,3.0},{-23.4,-0.3,28.0},{44.4,33.3,22.2},{-1.1,-2.2,-3.3}})));
    }

    @Test
    void testContiguousCompound() {
        Dataset contiguousCompound = hdfFile.getDatasetByPath("/contiguous_compound");
        // General checks
        assertThat(contiguousCompound.getDimensions(), is(equalTo(new int[]{5})));
        assertThat(contiguousCompound.getMaxSize(), is(equalTo(new int[]{-1})));
        assertThat(contiguousCompound.getJavaType(), is(Map.class));
        assertThat(contiguousCompound.getFillValue(), is(nullValue()));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) contiguousCompound.getData();
        assertThat(compoundData.size(), is(equalTo(3)));

        // Check the name element should be a string array
        String[] nameData = (String[]) compoundData.get("name");
        assertThat(nameData, is(equalTo(new String[]{"Bob","Peter","James","Sally","Ellie"})));

        // Check age element
        long[] ageData = (long[]) compoundData.get("age");
        assertThat(ageData, is(equalTo(new long[]{32,43,10,18,22})));

        // Check fav_number element
        double[] favNumData = (double[]) compoundData.get("fav_number");
        assertThat(favNumData, is(equalTo(new double[]{1.0,2.0,3.0,4.0,5.0})));

    }
}
