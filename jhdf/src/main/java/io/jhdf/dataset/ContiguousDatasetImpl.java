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
import io.jhdf.Utils;
import io.jhdf.api.Group;
import io.jhdf.api.dataset.ContiguousDataset;
import io.jhdf.exceptions.HdfException;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.jhdf.Constants.UNDEFINED_ADDRESS;

public class ContiguousDatasetImpl extends DatasetBase implements ContiguousDataset {

	final ContiguousDataLayoutMessage contiguousDataLayoutMessage;

	public ContiguousDatasetImpl(HdfBackingStorage hdfBackingStorage, long address, String name, Group parent, ObjectHeader oh) {
		super(hdfBackingStorage, address, name, parent, oh);
		this.contiguousDataLayoutMessage = getHeaderMessage(ContiguousDataLayoutMessage.class);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		try {
			ByteBuffer data = hdfBackingStorage.map(contiguousDataLayoutMessage.getAddress(), getSizeInBytes());
			convertToCorrectEndiness(data);
			return data;
		} catch (Exception e) {
			throw new HdfException("Failed to map data buffer for dataset '" + getPath() + "'", e);
		}
	}

	@Override
	public ByteBuffer getSliceDataBuffer(long[] sliceOffset, int[] sliceDimensions) {
		int totalElementsInSlice = Arrays.stream(sliceDimensions).reduce(1, Math::multiplyExact);

		ByteBuffer byteBuffer = ByteBuffer.allocate(totalElementsInSlice * getDataType().getSize());
		convertToCorrectEndiness(byteBuffer);

		long[] currentOffset = sliceOffset.clone();
		readSlicesToBuffer(-1, sliceOffset, sliceDimensions, currentOffset, byteBuffer);

		byteBuffer.flip();
		return byteBuffer;
	}

	private void readSlicesToBuffer(int dimensionIndex, long[] sliceOffset, int[] sliceDimensions, long[] currentOffset, ByteBuffer byteBuffer) {
		dimensionIndex++;
		if(dimensionIndex < (sliceDimensions.length - 1)) {
			// Not fastest dimension
			for (long i = 0; i < sliceDimensions[dimensionIndex]; i++) {
				currentOffset[dimensionIndex] = sliceOffset[dimensionIndex] + i;
				readSlicesToBuffer(dimensionIndex, sliceOffset, sliceDimensions, currentOffset, byteBuffer);
			}
		} else {
			// fastest dim so read some data
			long offsetBytes = Utils.dimensionIndexToLinearIndex(currentOffset, getDimensions()) * getDataType().getSize();
			long fileOffset = contiguousDataLayoutMessage.getAddress() + offsetBytes;

			final int fastestDimLengthBytes = sliceDimensions[sliceDimensions.length - 1] * getDataType().getSize();

			byteBuffer.put(hdfBackingStorage.readBufferFromAddress(fileOffset, fastestDimLengthBytes));
		}
	}

	@Override
	public ByteBuffer getBuffer() {
		return getDataBuffer();
	}

	@Override
	public long getDataAddress() {
		return contiguousDataLayoutMessage.getAddress();
	}

	@Override
	public boolean isEmpty() {
		return contiguousDataLayoutMessage.getAddress() == UNDEFINED_ADDRESS;
	}

	@Override
	public List<PipelineFilterWithData> getFilters() {
		return Collections.emptyList(); // Contiguous datasets don't have filters
	}
}
