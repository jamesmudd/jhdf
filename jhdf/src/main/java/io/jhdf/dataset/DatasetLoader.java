/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.ObjectHeader;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.dataset.chunked.ChunkedDatasetV3;
import io.jhdf.dataset.chunked.ChunkedDatasetV4;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;

public final class DatasetLoader {

	private DatasetLoader() {
		throw new AssertionError("No instances of DatasetLoader");
	}

	public static Dataset createDataset(HdfBackingStorage hdfBackingStorage, ObjectHeader oh, String name,
										Group parent) {

		final long address = oh.getAddress();
		try {
			// Determine the type of dataset to make
			final DataLayoutMessage dlm = oh.getMessageOfType(DataLayoutMessage.class);

			if (dlm instanceof CompactDataLayoutMessage) {
				return new CompactDataset(hdfBackingStorage, address, name, parent, oh);

			} else if (dlm instanceof ContiguousDataLayoutMessage) {
				return new ContiguousDatasetImpl(hdfBackingStorage, address, name, parent, oh);

			} else if (dlm instanceof ChunkedDataLayoutMessage) {
				return new ChunkedDatasetV3(hdfBackingStorage, address, name, parent, oh);

			} else if (dlm instanceof ChunkedDataLayoutMessageV4) {
				return new ChunkedDatasetV4(hdfBackingStorage, address, name, parent, oh);

			} else {
				throw new HdfException("Unrecognized Dataset layout type: " + dlm.getClass().getCanonicalName());
			}

		} catch (Exception e) {
			throw new HdfException("Failed to read dataset '" + name + "' at address '" + address + "'", e);
		}
	}

}
