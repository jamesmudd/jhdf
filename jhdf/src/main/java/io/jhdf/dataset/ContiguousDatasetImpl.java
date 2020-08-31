/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2020 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.api.dataset.ContiguousDataset;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;

import java.nio.ByteBuffer;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;

public class ContiguousDatasetImpl extends DatasetBase implements ContiguousDataset {

	public ContiguousDatasetImpl(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfFc, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ContiguousDataLayoutMessage contiguousDataLayoutMessage = getHeaderMessage(ContiguousDataLayoutMessage.class);

		// Check for empty dataset
		if (contiguousDataLayoutMessage.getAddress() == UNDEFINED_ADDRESS) {
			return null;
		}

		try {
			ByteBuffer data = hdfFc.map(contiguousDataLayoutMessage.getAddress(), getSizeInBytes());
			convertToCorrectEndiness(data);
			return data;
		} catch (Exception e) {
			throw new HdfException("Failed to map data buffer for dataset '" + getPath() + "'", e);
		}
	}

	@Override
	public ByteBuffer getBuffer() {
		return getDataBuffer();
	}

	@Override
	public long getDataAddress() {
		ContiguousDataLayoutMessage contiguousDataLayoutMessage = getHeaderMessage(ContiguousDataLayoutMessage.class);
		return contiguousDataLayoutMessage.getAddress();
	}

}
