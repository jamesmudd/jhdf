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

import static io.jhdf.Constants.UNDEFINED_ADDRESS;

import java.nio.ByteBuffer;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ContigiousDataLayoutMessage;

public class ContiguousDataset extends DatasetBase {

	public ContiguousDataset(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfFc, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ContigiousDataLayoutMessage contigiousDataLayoutMessage = getHeaderMessage(ContigiousDataLayoutMessage.class);

		// Check for empty dataset
		if (contigiousDataLayoutMessage.getAddress() == UNDEFINED_ADDRESS) {
			return null;
		}

		try {
			ByteBuffer data = hdfFc.map(contigiousDataLayoutMessage.getAddress(),
					contigiousDataLayoutMessage.getSize());
			convertToCorrectEndiness(data);
			return data;
		} catch (Exception e) {
			throw new HdfException("Failed to map data buffer for dataset '" + getPath() + "'", e);
		}
	}

}
