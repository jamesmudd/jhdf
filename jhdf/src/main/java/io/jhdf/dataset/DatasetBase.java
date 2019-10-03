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

import io.jhdf.AbstractNode;
import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.object.datatype.CompoundDataType;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.OrderedDataType;
import io.jhdf.object.datatype.VariableLength;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.FillValueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public abstract class DatasetBase extends AbstractNode implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(DatasetBase.class);

	protected final HdfFileChannel hdfFc;
	protected final ObjectHeader oh;

	private final DataType dataType;
	private final DataSpace dataSpace;

	public DatasetBase(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfFc, address, name, parent);
		this.hdfFc = hdfFc;
		this.oh = oh;

		dataType = getHeaderMessage(DataTypeMessage.class).getDataType();
		dataSpace = getHeaderMessage(DataSpaceMessage.class).getDataSpace();
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	protected void convertToCorrectEndiness(ByteBuffer bb) {
		if (dataType instanceof OrderedDataType) {
			final ByteOrder order = (((OrderedDataType) dataType).getByteOrder());
			bb.order(order);
			if(logger.isTraceEnabled()) {
				logger.trace("Set buffer order of '{}' to {}", getPath(), order);
			}
		} else {
			bb.order(LITTLE_ENDIAN);
		}
	}

	@Override
	public long getSize() {
		return dataSpace.getTotalLength();
	}

	@Override
	public long getDiskSize() {
		return getSize() * dataType.getSize();
	}

	@Override
	public int[] getDimensions() {
		return dataSpace.getDimensions();
	}

	@Override
	public int[] getMaxSize() {
		if (dataSpace.isMaxSizesPresent()) {
			return dataSpace.getMaxSizes();
		} else {
			return getDimensions();
		}
	}

	@Override
	public DataLayout getDataLayout() {
		return getHeaderMessage(DataLayoutMessage.class).getDataLayout();
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
	public Object getData() {
		logger.debug("Getting data for '{}'...", getPath());

		final ByteBuffer bb = getDataBuffer();
		if (bb == null) {
			// Empty
			return null;
		}

		final DataType type = getDataType();

		if (type instanceof VariableLength) {
			return VariableLengthDatasetReader.readDataset((VariableLength) type, bb,
					getDimensions(), hdfFc);
		} else if(type instanceof CompoundDataType) {
			return CompoundDatasetReader.readDataset((CompoundDataType) type, bb, getSize(), getDimensions());
		} else {
			return DatasetReader.readDataset(type, bb, getDimensions());
		}
	}

	@Override
	public boolean isScalar() {
		return getDimensions().length == 0;
	}

	@Override
	public boolean isEmpty() {
		return getDiskSize() == 0;
	}

	@Override
	public boolean isCompound() { return getDataType() instanceof CompoundDataType; }

	/**
	 * Gets the buffer that holds this datasets data. The returned buffer will be of
	 * the correct order (endiness).
	 *
	 * @return the data buffer that holds this dataset
	 */
	public abstract ByteBuffer getDataBuffer();

	@Override
	public Object getFillValue() {
		FillValueMessage fillValueMessage = getHeaderMessage(FillValueMessage.class);
		if (fillValueMessage.isFillValueDefined()) {
			ByteBuffer bb = fillValueMessage.getFillValue();
			// Convert to data pass zero length dims for scalar
			return DatasetReader.readDataset(getDataType(), bb, new int[0]);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "DatasetBase [path=" + getPath() + "]";
	}

}
