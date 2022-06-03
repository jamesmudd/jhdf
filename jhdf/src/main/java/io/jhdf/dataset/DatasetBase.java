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

import io.jhdf.AbstractNode;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.InvalidSliceHdfException;
import io.jhdf.filter.PipelineFilterWithData;
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
import io.jhdf.storage.HdfBackingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public abstract class DatasetBase extends AbstractNode implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(DatasetBase.class);

	protected final HdfBackingStorage hdfBackingStorage;
	protected final ObjectHeader oh;

	private final DataType dataType;
	private final DataSpace dataSpace;

	protected DatasetBase(HdfBackingStorage hdfBackingStorage, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfBackingStorage, address, name, parent);
		this.hdfBackingStorage = hdfBackingStorage;
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
			if (logger.isTraceEnabled()) {
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
	public long getSizeInBytes() {
		return getSize() * dataType.getSize();
	}

	@Override
	public int[] getDimensions() {
		return dataSpace.getDimensions();
	}

	@Override
	public long[] getMaxSize() {
		if (dataSpace.isMaxSizesPresent()) {
			return dataSpace.getMaxSizes();
		} else {
			return Arrays.stream(getDimensions()).asLongStream().toArray();
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

		if (isEmpty()) {
			return null;
		}

		final ByteBuffer bb = getDataBuffer();
		final DataType type = getDataType();

		return DatasetReader.readDataset(type, bb, getDimensions(), hdfBackingStorage);
	}

	@Override
	public Object getData(long[] sliceOffset, int[] sliceDimensions) {
		if (isEmpty()) {
			throw new HdfException("Cannot slice empty dataset");
		}
		if(isScalar()) {
			throw new HdfException("Cannot slice scalar dataset");
		}

		validateSliceRequest(sliceOffset, sliceDimensions);

		logger.debug("Getting data slice offset={} dimensions={} for [{}]'...", sliceOffset, sliceDimensions, getPath());
		ByteBuffer sliceDataBuffer = getSliceDataBuffer(sliceOffset, sliceDimensions);
		return DatasetReader.readDataset(getDataType(), sliceDataBuffer, sliceDimensions, hdfBackingStorage);
	}

	private void validateSliceRequest(long[] sliceOffset, int[] sliceDimensions) {
		final int numberOfDimensions = getDimensions().length;
		if (sliceOffset.length != numberOfDimensions
			|| sliceDimensions.length != numberOfDimensions
		) {
			throw new InvalidSliceHdfException("Requested slice does not match dataset dimensions", sliceOffset, sliceDimensions, getDimensions());
		}
		for (int i = 0; i < sliceOffset.length; i++) {
			if(sliceOffset[i] < 0) {
				throw new InvalidSliceHdfException("Requested sliceOffset has negative value in dimension: " + i, sliceOffset, sliceDimensions, getDimensions());
			}
			if(sliceDimensions[i] <= 0) {
				throw new InvalidSliceHdfException("Requested sliceDimensions has negative or zero value in dimension: " + i, sliceOffset, sliceDimensions, getDimensions());
			}

			if(sliceOffset[i] + sliceDimensions[i] > getDimensions()[i]) {
				throw new InvalidSliceHdfException("Requested slice exceeds dataset in dimension: " + i, sliceOffset, sliceDimensions, getDimensions());
			}
		}
	}

	@Override
	public boolean isScalar() {
		return getDimensions().length == 0;
	}

	@Override
	public boolean isEmpty() {
		return getSizeInBytes() == 0;
	}

	@Override
	public boolean isCompound() {
		return getDataType() instanceof CompoundDataType;
	}

	/**
	 * Gets the buffer that holds this datasets data. The returned buffer will be of
	 * the correct order (endiness).
	 *
	 * @return the data buffer that holds this dataset
	 */
	public abstract ByteBuffer getDataBuffer();


	public abstract ByteBuffer getSliceDataBuffer(long[] offset, int[] shape);

	@Override
	public Object getFillValue() {
		if(!getHeader().hasMessageOfType(FillValueMessage.class)) {
			return null; // No fill value message
		}
		FillValueMessage fillValueMessage = getHeaderMessage(FillValueMessage.class);
		if (fillValueMessage.isFillValueDefined()) {
			ByteBuffer bb = fillValueMessage.getFillValue();
			// Convert to data pass zero length dims for scalar
			return DatasetReader.readDataset(getDataType(), bb, new int[0], hdfBackingStorage);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "DatasetBase [path=" + getPath() + "]";
	}

	@Override
	public boolean isVariableLength() {
		return getDataType() instanceof VariableLength;
	}

	@Override
	public long getStorageInBytes() {
		return getSizeInBytes();
	}

}
