/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritiableDataset;
import io.jhdf.btree.BTreeV1;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.indexing.ChunkImpl;
import io.jhdf.exceptions.HdfWritingException;
import io.jhdf.filter.DeflatePipelineFilter;
import io.jhdf.filter.FilterManager;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.AttributeInfoMessage;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.FillValueMessage;
import io.jhdf.object.message.FilterPipelineMessage;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
// import java.lang.reflect.Array;
// import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jhdf.Utils.flatten;
// import static io.jhdf.Utils.stripLeadingIndex;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public class WritableDatasetImpl extends AbstractWritableNode implements WritiableDataset {

	private static final Logger logger = LoggerFactory.getLogger(WritableDatasetImpl.class);

	private final Object data;
	private DataType dataType;
	private boolean chunked=false;
	private int compression = 0;

	private final DataSpace dataSpace;

	public WritableDatasetImpl(Object data, String name, Group parent) {
		super(parent, name);

		// if (data.getClass() == io.jhdf.object.datatype.UFixed.class) {
		// 	this.data = ((UFixed) data).data;
		// 	this.dataType = DataType.fromObject(this.data);
		// 	this.dataSpace = DataSpace.fromObject(this.data);
		// 	((FixedPoint) this.dataType).setSigned(false);
		// 	logger.info("Class of dataset [{}] ", data.getClass());
		// } else {
			this.data = data;
			this.dataType = DataType.fromObject(data);
			this.dataSpace = DataSpace.fromObject(data);
		// }
	}

	@Override
	public long getSize() {
		return dataSpace.getTotalLength();
	}

	@Override
	public long getSizeInBytes() {
		return getSize() * dataType.getSize();
	}

	@Override
	public long getStorageInBytes() {
		// As there is no compression this is correct ATM
		return getSizeInBytes();
	}

	@Override
	public int[] getDimensions() {
		return dataSpace.getDimensions();
	}

	@Override
	public boolean isScalar() {
		if (isEmpty()) {
			return false;
		}
		return getDimensions().length == 0;
	}

	@Override
	public boolean isEmpty() {
		return data == null;
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
		return dataSpace.getMaxSizes();
	}

	@Override
	public DataLayout getDataLayout() {
		// ATM we only support contiguous
		return DataLayout.CONTIGUOUS;
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public Object getDataFlat() {
		return flatten(data);
	}

	@Override
	public Object getData(long[] sliceOffset, int[] sliceDimensions) {
		throw new HdfWritingException("Slicing a writable dataset not supported");
	}

	@Override
	public Class<?> getJavaType() {
		final Class<?> type = dataType.getJavaType();
		// For scalar datasets the returned type will be the wrapper class because
		// getData returns Object
		if (isScalar() && type.isPrimitive()) {
			return primitiveToWrapper(type);
		}
		return type;
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
		// ATM no filters support
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
		return getParent().getFile();
	}

	@Override
	public Path getFileAsPath() {
		return getParent().getFileAsPath();
	}

	@Override
	public HdfFile getHdfFile() {
		return getParent().getHdfFile();
	}

	@Override
	public long getAddress() {
		throw new HdfWritingException("Address not known until written");
	}

	@Override
	public void setChunked(boolean chunk) {
		chunked = chunk;
	}

	@Override
	public void setCompression(int compress) {
		compression = compress;
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
		if (chunked) {
			return writec(hdfFileChannel, position);
		} else {
			return writes(hdfFileChannel, position);
		}
	}


	private long writec(HdfFileChannel hdfFileChannel, long position) {

		logger.info("Writing chunked dataset [{}] at position [{}]", getPath(), position);
		List<Message> messages = new ArrayList<>();
		//messages.add(DataSpaceMessage.create(this.dataSpace));
		messages.add(DataSpaceMessage.create(DataSpace.fromObjectV1(this.data)));
		messages.add(DataTypeMessage.create(this.dataType));
		messages.add(FillValueMessage.NO_FILL);

		if (compression > 0) {
			messages.add(FilterPipelineMessage.create());
		}

		if (!getAttributes().isEmpty()) {
			AttributeInfoMessage attributeInfoMessage = AttributeInfoMessage.create();
			messages.add(attributeInfoMessage);
			for (Map.Entry<String, Attribute> attribute : getAttributes().entrySet()) {
				logger.info("Writing attribute [{}]", attribute.getKey());
				AttributeMessage attributeMessage = AttributeMessage.create(attribute.getKey(), attribute.getValue());
				messages.add(attributeMessage);
			}
		}

		int elemSize = this.dataType.getSize();		
		long bTreeAddress = position + 0x100;

		//Data layout message
		messages.add(ChunkedDataLayoutMessage.create(bTreeAddress, elemSize, Utils.getDimensions(data)));


		// Write Object Header
		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
		hdfFileChannel.write(objectHeader.toBuffer(), position);

		// Write data to get length of chunk
		long dataAddress = bTreeAddress + 0x1000;
		int dataSize = (int) writeData(hdfFileChannel, dataAddress);

		//Construct and Write BTree
		ArrayList<Chunk> chunks = new ArrayList<>(1);
		int[] chunkOffset = new int[Utils.getDimensions(data).length];
		BitSet filterMask = BitSet.valueOf(new byte[] { 0, 0, 0, 0 });
		chunks.add(new ChunkImpl(dataAddress, dataSize, chunkOffset, filterMask));
		chunks.add(new ChunkImpl(0, 0, getDimensions(), filterMask));

		BTreeV1 bTree = BTreeV1.createDataBTree(Constants.UNDEFINED_ADDRESS,Constants.UNDEFINED_ADDRESS,dataType,chunks);

		hdfFileChannel.write(bTree.toBuffer(), bTreeAddress);	

		// Done
		return dataAddress + dataSize;
	}


	private long writes(HdfFileChannel hdfFileChannel, long position) {
		logger.info("Writing contiguous dataset [{}] at position [{}]", getPath(), position);
		List<Message> messages = new ArrayList<>();
		messages.add(DataTypeMessage.create(this.dataType));
		messages.add(DataSpaceMessage.create(this.dataSpace));
		messages.add(FillValueMessage.NO_FILL);

		if (!getAttributes().isEmpty()) {
			AttributeInfoMessage attributeInfoMessage = AttributeInfoMessage.create();
			messages.add(attributeInfoMessage);
			for (Map.Entry<String, Attribute> attribute : getAttributes().entrySet()) {
				logger.info("Writing attribute [{}]", attribute.getKey());
				AttributeMessage attributeMessage = AttributeMessage.create(attribute.getKey(), attribute.getValue());
				messages.add(attributeMessage);
			}
		}

		// TODO will have know fixed size so don't really need these objects but for now...
		ContiguousDataLayoutMessage placeholder =  ContiguousDataLayoutMessage.create(Constants.UNDEFINED_ADDRESS,
			Constants.UNDEFINED_ADDRESS);
		messages.add(placeholder);

		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
		int ohSize = objectHeader.toBuffer().limit();

		objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
	

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

		hdfFileChannel.position(dataAddress);
		if (chunked) {
			ByteBuffer buffer;
			if (compression > 0 && compression <=9) {
				DeflatePipelineFilter filter = (DeflatePipelineFilter) FilterManager.getFilter(1);		
				int[] filterData = {compression};

				buffer = ByteBuffer.wrap(filter.encode(dataType.encodeData(data).array(),filterData));	
			} else {
				buffer = dataType.encodeData(data);
			}	

			hdfFileChannel.write(buffer,dataAddress);
			return buffer.limit();
		} else  {
			dataType.writeData(data, getDimensions(), hdfFileChannel);
			return dataSpace.getTotalLength() * dataType.getSize();
		}
	}

	// private static void writeDoubleData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel) {
	// 	if (dims.length > 1) {
	// 		for (int i = 0; i < dims[0]; i++) {
	// 			Object newArray = Array.get(data, i);
	// 			writeDoubleData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel);
	// 		}
	// 	} else {
	// 		buffer.asDoubleBuffer().put((double[]) data);
	// 		hdfFileChannel.write(buffer);
	// 		buffer.clear();
	// 	}
	// }
}
