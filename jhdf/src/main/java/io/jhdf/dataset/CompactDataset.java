/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

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

	@Override
	public ByteBuffer getSliceDataBuffer(long[] offset, int[] shape) {
		throw new UnsupportedHdfException("Compact datasets don't support slice reading");
	}

	public List<PipelineFilterWithData> getFilters() {
		return Collections.emptyList(); // Compact datasets don't have filters
	}
}
