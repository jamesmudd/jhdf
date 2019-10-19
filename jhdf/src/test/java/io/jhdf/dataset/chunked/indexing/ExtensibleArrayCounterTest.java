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
        // multiplier = 1, # blocks = 1
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(1)));
        // multiplier = 2, # blocks = 1
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(2)));
        // multiplier = 2, # blocks = 2
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(2)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(2)));
        // multiplier = 4, # blocks = 2
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        // multiplier = 4, # blocks = 4
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(4)));
        // multiplier = 8, # blocks = 4
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        // multiplier = 8, # blocks = 8
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(8)));
        // multiplier = 16, # blocks = 8
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(16)));
    }

    @Test
    void testExtensibleArrayCounterWith16() {
        ExtensibleArrayIndex.ExtensibleArrayCounter eac = new ExtensibleArrayIndex.ExtensibleArrayCounter(16);
        // multiplier = 1, # blocks = 1
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(16)));
        // multiplier = 2, # blocks = 1
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(32)));
        // multiplier = 2, # blocks = 2
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(32)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(32)));
        // multiplier = 4, # blocks = 2
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(64)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(64)));
        // multiplier = 4, # blocks = 4
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(64)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(64)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(64)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(64)));
        // multiplier = 8, # blocks = 4
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        // multiplier = 8, # blocks = 8
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(128)));
        // multiplier = 16, # blocks = 8
        assertThat(eac.getNextNumberOfChunks(), is(equalTo(256)));
    }

    @Test
    void testExtensibleArrayCounterToString() {
        ExtensibleArrayIndex.ExtensibleArrayCounter eac = new ExtensibleArrayIndex.ExtensibleArrayCounter(16);
        assertThat(eac.toString(), is(equalTo("ExtensibleArrayCounter{minNumberOfElementsInDataBlock=16, blockSizeMultiplier=1, numberOfBlocks=1, blockCounter=0, increaseNumberOfBlocksNext=false}")));
    }
}
