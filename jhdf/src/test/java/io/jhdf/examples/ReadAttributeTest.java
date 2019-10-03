/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import org.junit.jupiter.api.Test;

public class ReadAttributeTest {

    @Test
    public void testReadingAttributeEarliest() {
        ReadAttribute.main(new String[]{"src/test/resources/io/jhdf/test_attribute_earliest.hdf5", "/test_group/data", "2d_string"});
    }

    @Test
    public void testReadingAttributeLatest() {
		ReadAttribute.main(new String[]{"src/test/resources/io/jhdf/test_attribute_latest.hdf5", "/test_group/data", "2d_string"});
    }

}
