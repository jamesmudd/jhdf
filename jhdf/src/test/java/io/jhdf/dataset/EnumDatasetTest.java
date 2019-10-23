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
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.object.datatype.EnumDataType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.flatten;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class EnumDatasetTest {

    private static final String ENUM_EARLIEST_TEST_FILE_NAME = "../test_enum_datasets_earliest.hdf5";
    private static final String ENUM_LATEST_TEST_FILE_NAME = "../test_enum_datasets_latest.hdf5";

    private static HdfFile earliesthdfFile;
    private static HdfFile latesthdfFile;

    @BeforeAll
    static void setup() {
        String testFileUrl = ChunkedDatasetTest.class.getResource(ENUM_EARLIEST_TEST_FILE_NAME).getFile();
        earliesthdfFile = new HdfFile(new File(testFileUrl));
        testFileUrl = ChunkedDatasetTest.class.getResource(ENUM_LATEST_TEST_FILE_NAME).getFile();
        latesthdfFile = new HdfFile(new File(testFileUrl));
    }

    @TestFactory
    Stream<DynamicNode> earliest() throws Exception {
        List<Dataset> datasets = new ArrayList<>();
        getAllDatasets(earliesthdfFile, datasets);

        return datasets.stream().map(this::verifyDataset);
    }

    @TestFactory
    Stream<DynamicNode> latest() throws Exception {
        List<Dataset> datasets = new ArrayList<>();
        getAllDatasets(latesthdfFile, datasets);

        return datasets.stream().map(this::verifyDataset);
    }

    private void getAllDatasets(Group group, List<Dataset> datasets) {
        for (Node node : group) {
            if (node instanceof Group) {
                Group group2 = (Group) node;
                getAllDatasets(group2, datasets);
            } else if (node instanceof Dataset) {
                datasets.add((Dataset) node);
            }
        }
    }

    private DynamicTest verifyDataset(Dataset dataset) {
        return dynamicTest(dataset.getPath(), () -> {
            if(dataset.getName().startsWith("2d")) {
                assertThat(dataset.getDimensions(), is(equalTo(new int[]{2, 2})));
            } else {
                assertThat(dataset.getDimensions(), is(equalTo(new int[]{4})));
            }
            assertThat(dataset.getJavaType(), is(String.class));
            assertThat(dataset.getDataType(), is(instanceOf(EnumDataType.class)));
            Object data = dataset.getData();
            Object[] flatData = flatten(data);
            assertThat(flatData, is(arrayContaining(
                    "RED",
                    "GREEN",
                    "BLUE",
                    "YELLOW")));

        });
    }


}
