/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritiableDataset;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.FillValueMessage;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jhdf.Utils.stripLeadingIndex;

public class WritableDatasetImpl extends AbstractWritableNode implements WritiableDataset {

	private static final Logger logger = LoggerFactory.getLogger(WritableDatasetImpl.class);

	private final Object data;
	private final DataType dataType;

	private final DataSpace dataSpace;

	public WritableDatasetImpl(Object data, String name, Group parent) {
		super(parent, name);
		this.data = data;
		this.dataType = DataType.fromObject(data);
		this.dataSpace = DataSpace.fromObject(data);
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
		logger.info("Writing dataset [{}] at position [{}]", getPath(), position);
		List<Message> messages = new ArrayList<>();
		messages.add(DataTypeMessage.create(this.dataType));
		messages.add(DataSpaceMessage.create(this.dataSpace));
		messages.add(FillValueMessage.NO_FILL);
		// TODO will have know fixed size so don't really need these objects but for now...
		ContiguousDataLayoutMessage placeholder = ContiguousDataLayoutMessage.create(Constants.UNDEFINED_ADDRESS, Constants.UNDEFINED_ADDRESS);
		messages.add(placeholder);

		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
		int ohSize = objectHeader.toBuffer().limit();

		// Now know where we will write the data
		long dataAddress = position + ohSize;
		long dataSize = writeData(hdfFileChannel, dataAddress);

		// Now switch placeholder for real data layout message
		messages.add(ContiguousDataLayoutMessage.create(dataAddress, dataSize));
		messages.remove(placeholder);

		objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);

		hdfFileChannel.write(objectHeader.toBuffer(), position);

		return dataAddress + dataSize;
	}

	private long writeData(HdfFileChannel hdfFileChannel, long dataAddress) {
		logger.info("Writing data for dataset [{}] at position [{}]", getPath(), dataAddress);
		Class<?> arrayType = Utils.getArrayType(this.data);
		long totalBytes = dataSpace.getTotalLength() * dataType.getSize();

		int[] dimensions = dataSpace.getDimensions();
		int fastDimSize = dimensions[dimensions.length - 1];
		ByteBuffer buffer = ByteBuffer.allocate(fastDimSize * dataType.getSize()).order(ByteOrder.nativeOrder());
		hdfFileChannel.position(dataAddress);

		// TODO move out into data types?
		if(arrayType.equals(int.class)) {
			writeIntData(data, dimensions, buffer, hdfFileChannel);
		} else if (arrayType.equals(double.class)) {
			writeDoubleData(data, dimensions, buffer, hdfFileChannel);
		} else {
			throw new UnsupportedHdfException("Writing [" + arrayType.getSimpleName() + "] is not supported");
		}
		return totalBytes;
	}

	private static void writeIntData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeIntData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel);
			}
		} else {
			buffer.asIntBuffer().put((int[]) data);
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}
	private static void writeDoubleData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeDoubleData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel);
			}
		} else {
			buffer.asDoubleBuffer().put((double[]) data);
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}
}
