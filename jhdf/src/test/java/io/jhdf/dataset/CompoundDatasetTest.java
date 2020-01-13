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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
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
        assertThat(dataset.getDimensions(), is(equalTo(new int[]{3, 3})));
        assertThat(dataset.getMaxSize(), is(equalTo(new int[]{3, 3})));
        assertThat(dataset.getJavaType(), is(Map.class));
        assertThat(dataset.getFillValue(), is(nullValue()));
        assertThat(dataset.isCompound(), is(true));

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
        assertThat(dataset.isCompound(), is(true));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) dataset.getData();
        assertThat(compoundData.size(), is(equalTo(6)));

        // Check the first name element should be a string array
        String[] firstNames = (String[]) compoundData.get("firstName");
        assertThat(firstNames, is(equalTo(new String[]{"Bob", "Peter", "James", "Ellie"})));

        String[] surnames = (String[]) compoundData.get("surname");
        assertThat(surnames, is(equalTo(new String[]{"Smith", "Fletcher", "Mudd", "Kyle"})));

        // Test enum type
        String[] genders = (String[]) compoundData.get("gender");
        assertThat(genders, is(equalTo(new String[]{"MALE", "MALE", "MALE", "FEMALE"})));

        int[] ages = (int[]) compoundData.get("age");
        assertThat(ages, is(equalTo(new int[]{32, 43, 12, 22})));

        float[] favNumber = (float[]) compoundData.get("fav_number");
        assertThat(favNumber, is(equalTo(new float[]{1.0f, 2.0f, 3.0f, 4.0f})));

        // Check vector element
        float[][] doubleData = (float[][]) compoundData.get("vector");
        assertThat(doubleData, is(equalTo(new float[][]{
                {1.0f, 2.0f, 3.0f},
                {16.2f, 2.2f, -32.4f},
                {-32.1f, -774.1f, -3.0f},
                {2.1f, 74.1f, -3.8f}
        })));
    }

    private static Stream<Arguments> testArrayVariableLength() {
        return Stream.of(
                Arguments.of(earliestHdfFile.getDatasetByPath("/array_vlen_chunked_compound")),
                Arguments.of(earliestHdfFile.getDatasetByPath("/array_vlen_contigious_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/array_vlen_chunked_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/array_vlen_contigious_compound"))
        );
    }

    @ParameterizedTest
    @MethodSource("testArrayVariableLength")
    void testArrayVariableLength(Dataset dataset) {
        assertThat(dataset.getDimensions(), is(equalTo(new int[]{1})));
        assertThat(dataset.getMaxSize(), is(equalTo(new int[]{1})));
        assertThat(dataset.isCompound(), is(true));
        assertThat(dataset.getJavaType(), is(Map.class));
        assertThat(dataset.getFillValue(), is(nullValue()));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) dataset.getData();
        assertThat(compoundData.size(), is(equalTo(1)));

        // Check the first name element should be a string array
        String[][] names = (String[][]) compoundData.get("name");
        assertThat(names.length, is(1));
        assertThat(names[0], is(equalTo(new String[]{"James", "Ellie"})));
    }

    private static Stream<Arguments> testVariableLengthCompound() {
        return Stream.of(
                Arguments.of(earliestHdfFile.getDatasetByPath("/vlen_chunked_compound")),
                Arguments.of(earliestHdfFile.getDatasetByPath("/vlen_contigious_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/vlen_chunked_compound")),
                Arguments.of(latestHdfFile.getDatasetByPath("/vlen_contigious_compound"))
        );
    }

    @ParameterizedTest
    @MethodSource("testVariableLengthCompound")
    void testVariableLengthCompound(Dataset dataset) {
        assertThat(dataset.getDimensions(), is(equalTo(new int[]{3})));
        assertThat(dataset.getMaxSize(), is(equalTo(new int[]{3})));
        assertThat(dataset.isCompound(), is(true));
        assertThat(dataset.getJavaType(), is(Map.class));
        assertThat(dataset.getFillValue(), is(nullValue()));

        // Check the compoundness
        Map<String, Object> compoundData = (Map<String, Object>) dataset.getData();
        assertThat(compoundData.keySet(), contains("one", "two"));

        Object[] oneData = (Object[]) compoundData.get("one");
        assertThat(oneData.length, is(3));
        assertThat(toObject((int[]) oneData[0]), is(arrayContaining(1)));
        assertThat(toObject((int[]) oneData[1]), is(arrayContaining(1, 1)));
        assertThat(toObject((int[]) oneData[2]), is(arrayContaining(1, 1, 1)));

        Object[] twoData = (Object[]) compoundData.get("two");
        assertThat(twoData.length, is(3));
        assertThat(toObject((int[]) twoData[0]), is(arrayContaining(2)));
        assertThat(toObject((int[]) twoData[1]), is(arrayContaining(2, 2)));
        assertThat(toObject((int[]) twoData[2]), is(arrayContaining(2, 2, 2)));
    }

}
