/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ChunkedV4DatasetTest {

    private static final String HDF5_TEST_FILE_NAME = "../chunked_v4_datasets.hdf5";

    private static HdfFile hdfFile;

    @BeforeAll
    static void setup() {
        String testFileUrl = ChunkedDatasetTest.class.getResource(HDF5_TEST_FILE_NAME).getFile();
        hdfFile = new HdfFile(new File(testFileUrl));
    }

    @TestFactory
    List<DynamicNode> allDatasets() throws Exception {
        List<Dataset> datasets = new ArrayList<>();
        getAllDatasets(hdfFile, datasets);

        return datasets.stream().map(dataset -> dynamicTest(dataset.getPath(), () -> System.out.println(dataset))).collect(Collectors.toList());
        //return dynamicTest(path.getFileName().toString(), () -> this::verifyDataset);
    }

    private void verifyDataset(Dataset dataset) {
        dataset.getSize();
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
}
