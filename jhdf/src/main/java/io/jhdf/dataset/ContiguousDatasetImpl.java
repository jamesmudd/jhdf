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
import io.jhdf.Utils;
import io.jhdf.api.Group;
import io.jhdf.api.dataset.ContiguousDataset;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.InvalidSliceHdfException;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
		final int numberOfDimensions = getDimensions().length;
		if (sliceOffset.length != numberOfDimensions || sliceDimensions.length != numberOfDimensions) {
			// TODO exception message etc
			throw new InvalidSliceHdfException("TODO", sliceOffset, sliceDimensions, getDimensions());
		}

		int totalElementsInSlice = Arrays.stream(sliceDimensions).reduce(1, Math::multiplyExact);

		ByteBuffer byteBuffer = ByteBuffer.allocate(totalElementsInSlice * getDataType().getSize());
		convertToCorrectEndiness(byteBuffer);

		long offsetBytes = Utils.dimensionIndexToLinearIndex(sliceOffset, getDimensions()) * getDataType().getSize();
		long fileOffset = contiguousDataLayoutMessage.getAddress() + offsetBytes;

		final int fastestDimLengthBytes = sliceDimensions[sliceDimensions.length - 1] * getDataType().getSize();

		int sectionsToRead = 1;
		for (int i = 0; i < sliceDimensions.length - 1; i++) {
			sectionsToRead += sliceDimensions[i] - 1;
		}
		int sizeToSkip = getDimensions()[sliceDimensions.length - 1] * getDataType().getSize() - fastestDimLengthBytes;

		for (int i = 0; i < sectionsToRead; i++) {
			byteBuffer.put(hdfBackingStorage.readBufferFromAddress(fileOffset, fastestDimLengthBytes));
			fileOffset += sizeToSkip;
		}

		byteBuffer.flip();
		return byteBuffer;
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
}
