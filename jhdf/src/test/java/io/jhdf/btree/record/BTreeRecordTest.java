/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.btree.record;

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BTreeRecordTest {

    @Test
    void testUnsupportedRecordTypesThrow() {
        assertThrows(HdfException.class, () -> BTreeRecord.readRecord(0, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(1, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(2, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(3, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(4, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(6, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(7, null, null));
        assertThrows(UnsupportedHdfException.class, () -> BTreeRecord.readRecord(9, null, null));
    }

    @Test
    void testUnreconizedRecordTypeThrows() {
        assertThrows(HdfException.class, () -> BTreeRecord.readRecord(63, null, null));
    }
}
