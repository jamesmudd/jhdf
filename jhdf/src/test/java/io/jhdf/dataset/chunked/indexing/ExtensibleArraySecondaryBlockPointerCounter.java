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

class ExtensibleArraySecondaryBlockPointerCounter {

    @Test
    void testExtensibleArrayCounter() {
        ExtensibleArrayIndex.ExtensibleArraySecondaryBlockPointerCounter sbpc =
                new ExtensibleArrayIndex.ExtensibleArraySecondaryBlockPointerCounter(1);
        // multiplier = 1
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(1)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(1)));
        // multiplier = 2
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(2)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(2)));
        // multiplier = 4
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(4)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(4)));
        // multiplier = 8
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(8)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(8)));
        // multiplier = 16
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(16)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(16)));
        // multiplier = 32
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(32)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(32)));
    }

    @Test
    void testExtensibleArrayCounterWith4() {
        ExtensibleArrayIndex.ExtensibleArraySecondaryBlockPointerCounter sbpc =
                new ExtensibleArrayIndex.ExtensibleArraySecondaryBlockPointerCounter(4);
        // multiplier = 1
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(4)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(4)));
        // multiplier = 2
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(8)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(8)));
        // multiplier = 4
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(16)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(16)));
        // multiplier = 8
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(32)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(32)));
        // multiplier = 16
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(64)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(64)));
        // multiplier = 32
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(128)));
        assertThat(sbpc.getNextNumberOfPointers(), is(equalTo(128)));
    }

}
