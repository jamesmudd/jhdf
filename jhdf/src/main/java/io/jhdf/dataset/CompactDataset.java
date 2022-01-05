/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;

public class CompactDataset extends DatasetBase {

	public CompactDataset(HdfBackingStorage hdfBackingStorage, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfBackingStorage, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ByteBuffer data = getHeaderMessage(CompactDataLayoutMessage.class).getDataBuffer();
		convertToCorrectEndiness(data);
		return data;
	}

}
