/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import java.nio.ByteBuffer;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;

public class CompactDataset extends DatasetBase {

	public CompactDataset(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfFc, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ByteBuffer data = getHeaderMessage(CompactDataLayoutMessage.class).getDataBuffer();
		convertToCorrectEndiness(data);
		return data;
	}

}
