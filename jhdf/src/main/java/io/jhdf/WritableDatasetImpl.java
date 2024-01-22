/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritiableDataset;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfFileChannel;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WritableDatasetImpl extends AbstractWritableNode implements WritiableDataset {

	private final Object data;
	private final DataType dataType;

	public WritableDatasetImpl(Object data, String name, Group parent) {
		super(parent, name);
		this.data = data;
		this.dataType = DataType.fromObject(data);
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public long getSizeInBytes() {
		return 0;
	}

	@Override
	public long getStorageInBytes() {
		return 0;
	}

	@Override
	public int[] getDimensions() {
		return Utils.getDimensions(data);
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public boolean isVariableLength() {
		return false;
	}

	@Override
	public long[] getMaxSize() {
		return new long[0];
	}

	@Override
	public DataLayout getDataLayout() {
		return null;
	}

	@Override
	public Object getData() {
		return null;
	}

	@Override
	public Object getDataFlat() {
		return null;
	}

	@Override
	public Object getData(long[] sliceOffset, int[] sliceDimensions) {
		return null;
	}

	@Override
	public Class<?> getJavaType() {
		return Utils.getArrayType(data);
	}

	@Override
	public DataType getDataType() {
		return dataType;
	}

	@Override
	public Object getFillValue() {
		return null;
	}

	@Override
	public List<PipelineFilterWithData> getFilters() {
		return Collections.emptyList();
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public Path getFileAsPath() {
		return null;
	}

	@Override
	public HdfFile getHdfFile() {
		return null;
	}

	@Override
	public long getAddress() {
		return 0;
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return false;
	}

	@Override
	public long write(HdfFileChannel hdfFileChannel, long position) {
		List<Message> messages = new ArrayList<>();
		messages.add(DataTypeMessage.create(this.dataType));

		ContiguousDataLayoutMessage layoutMessage = ContiguousDataLayoutMessage.create(1L, 1L);

		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
		hdfFileChannel.write(objectHeader.toBuffer(), position);

		return 0;


//		throw new UnsupportedOperationException();
	}
}
