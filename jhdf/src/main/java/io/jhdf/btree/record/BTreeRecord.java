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

import io.jhdf.dataset.chunked.DatasetInfo;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;

public abstract class BTreeRecord {

	@SuppressWarnings("unchecked") // Requires that the b-tree is of the correct type for the record
	public static <T extends BTreeRecord> T readRecord(int type, ByteBuffer buffer, DatasetInfo datasetInfo) {
		switch (type) {
			case 0:
				throw new HdfException("b-tree record type 0. Should only be used for testing");
			case 1:
				throw new UnsupportedHdfException("b-tree record type 1. Currently not supported");
			case 2:
				throw new UnsupportedHdfException("b-tree record type 2. Currently not supported");
			case 3:
				throw new UnsupportedHdfException("b-tree record type 3. Currently not supported");
			case 4:
				throw new UnsupportedHdfException("b-tree record type 4. Currently not supported");
			case 5:
				return (T) new LinkNameForIndexedGroupRecord(buffer);
			case 6:
				throw new UnsupportedHdfException("b-tree record type 6. Currently not supported");
			case 7:
				throw new UnsupportedHdfException("b-tree record type 7. Currently not supported");
			case 8:
				return (T) new AttributeNameForIndexedAttributesRecord(buffer);
			case 9:
				throw new UnsupportedHdfException("b-tree record type 9. Currently not supported");
			case 10:
				return (T) new NonFilteredDatasetChunks(buffer, datasetInfo);
			case 11:
				return (T) new FilteredDatasetChunks(buffer, datasetInfo);
			default:
				throw new HdfException("Unknown b-tree record type. Type = " + type);
		}
	}

}
