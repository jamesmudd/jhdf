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
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.hamcrest.Matchers;
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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ChunkedV4DatasetTest {

    private static final String HDF5_TEST_FILE_NAME = "../chunked_v4_datasets.hdf5";

    private static HdfFile hdfFile;

    @BeforeAll
    static void setup() {
        String testFileUrl = ChunkedDatasetTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();
        hdfFile = new HdfFile(new File(testFileUrl));
    }

    @TestFactory
    Stream<DynamicNode> verifyDatasets() throws Exception {
        List<Dataset> datasets = new ArrayList<>();
        getAllDatasets(hdfFile, datasets);

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
            if(dataset.getName().startsWith("large")) {
                assertThat(dataset.getDimensions(), is(equalTo(new int[]{200, 5, 10})));
            } else {
                assertThat(dataset.getDimensions(), is(equalTo(new int[]{5, 3})));
            }
            Object data = dataset.getData();
            Object[] flatData = flatten(data);
            for (int i = 0; i < flatData.length; i++) {
                // Do element comparison as there are all different primitive numeric types
                assertThat(Double.valueOf(flatData[i].toString()), is(equalTo((double) i)));
            }
        });
    }


}
