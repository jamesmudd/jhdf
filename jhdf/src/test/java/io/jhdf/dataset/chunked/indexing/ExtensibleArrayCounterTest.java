/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ExtensibleArrayCounterTest {

    @Test
    void testExtensibleArrayCounter() {
        ExtensibleArrayIndex.ExtensibleArrayCounter eac = new ExtensibleArrayIndex.ExtensibleArrayCounter(1);
        // multipler = 1, # blocks = 1
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(1)));
        // multipler = 2, # blocks = 1
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(2)));
        // multipler = 2, # blocks = 2
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(2)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(2)));
        // multipler = 4, # blocks = 2
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        // multipler = 4, # blocks = 4
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        // multipler = 8, # blocks = 4
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        // multipler = 8, # blocks = 8
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        // multipler = 16, # blocks = 8
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(16)));
    }
}
