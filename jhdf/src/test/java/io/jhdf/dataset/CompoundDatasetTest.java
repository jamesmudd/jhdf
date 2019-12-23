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
import io.jhdf.api.Dataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class CompoundDatasetTest {

    private static final String HDF5_TEST_EARLIEST_FILE_NAME = "compound_datasets_earliest.hdf5";
    private static final String HDF5_TEST_LATEST_FILE_NAME = "compound_datasets_latest.hdf5";

    private static HdfFile earliestHdfFile;
    private static HdfFile latestHdfFile;

    @BeforeAll
    static void setup() throws Exception {
        earliestHdfFile = loadTestHdfFile(HDF5_TEST_EARLIEST_FILE_NAME);
        latestHdfFile = loadTestHdfFile(HDF5_TEST_LATEST_FILE_NAME);
    }

    @AfterAll
    static void tearDown() {
        earliestHdfFile.close();
        latestHdfFile.close();
    }

    private static Stream<Arguments> test2dCompound() {
        return Stream.of(
                Arguments.of(earliestHdfFile.getDatasetByPath("/2d_chunked_compound")),
                Arguments.of(earliestHdfFile.getDatasetByPath("/2d_contigious_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/2d_chunked_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/2d_contigious_compound"))
        );
    }

    @ParameterizedTest
    @MethodSource("test2dCompound")
    void test2dCompound(Dataset dataset) {
        // General checks
        assertThat(dataset.getDimensions(), is(equalTo(new int[]{3,3})));
        assertThat(dataset.getMaxSize(), is(equalTo(new int[]{3,3})));
        assertThat(dataset.getJavaType(), is(Map.class));
        assertThat(dataset.getFillValue(), is(nullValue()));

        // Check the compoundness
        @SuppressWarnings("unchecked")
        Map<String, Object> compoundData = (Map<String, Object>) dataset.getData();
        assertThat(compoundData.size(), is(equalTo(2)));

        // Check the real element should be a 3x3 array
        float[][] realData = (float[][]) compoundData.get("real");
        assertThat(realData, is(equalTo(new float[][]{
                {2.3f, 12.3f, -32.3f},
                {2.3f, 12.3f, -32.3f},
                {2.3f, 12.3f, -32.3f},
        })));

        // Check img element
        float[][] imgData = (float[][]) compoundData.get("img");
        assertThat(imgData, is(equalTo(new float[][]{
                {-7.3f, -17.3f, -0.3f},
                {-7.3f, -17.3f, -0.3f},
                {-7.3f, -17.3f, -0.3f}
        })));
    }

    private static Stream<Arguments> testCompound() {
        return Stream.of(
                Arguments.of(earliestHdfFile.getDatasetByPath("/chunked_compound")),
                Arguments.of(earliestHdfFile.getDatasetByPath("/contigious_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/chunked_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/contigious_compound"))
        );
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("testCompound")
    void testCompound(Dataset dataset) {
        // General checks
        assertThat(dataset.getDimensions(), is(equalTo(new int[]{4})));
        assertThat(dataset.getMaxSize(), is(equalTo(new int[]{4})));
        assertThat(dataset.getJavaType(), is(Map.class));
        assertThat(dataset.getFillValue(), is(nullValue()));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) dataset.getData();
        assertThat(compoundData.size(), is(equalTo(2)));

        // Check the name element should be a string array
        String[] nameData = (String[]) compoundData.get("name");
        assertThat(nameData, is(equalTo(new String[]{"one", "two", "three", "four"})));

        // Check vector element
        double[][] doubleData = (double[][]) compoundData.get("vector");
        assertThat(doubleData, is(equalTo(new double[][]{{1.0,2.0,3.0},{-23.4,-0.3,28.0},{44.4,33.3,22.2},{-1.1,-2.2,-3.3}})));
    }

}
