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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RawChunkAccessTest {

    @Test
    void testChunkedV4() {
        assertDoesNotThrow(() ->
                RawChunkAccess.main(new String[]{"src/test/resources/hdf5/chunked_v4_datasets.hdf5",
                        "/filtered_btree_v2/float32"}));
    }

    @Test
    void testChunkedEarliest() {
        assertDoesNotThrow(() ->
                RawChunkAccess.main(new String[]{"src/test/resources/hdf5/test_chunked_datasets_earliest.hdf5",
                        "/int/int8"}));
    }

    @Test
    void testChunkedLatest() {
        assertDoesNotThrow(() ->
                RawChunkAccess.main(new String[]{"src/test/resources/hdf5/test_chunked_datasets_earliest.hdf5",
                        "/float/float64"}));
    }
}
