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

import io.jhdf.GlobalHeap;
import io.jhdf.HdfFileChannel;
import io.jhdf.Utils;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.VariableLength;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public final class VariableLengthDatasetReader {

	/** No instances */
	private VariableLengthDatasetReader() {
	}

	public static Object readDataset(VariableLength type, ByteBuffer buffer, int[] dimensions, HdfFileChannel hdfFc) {
		// Make the array to hold the data
		Class<?> javaType = type.getJavaType();

		// If the data is scalar make a fake one element array then remove it at the end
		final Object data;
		final boolean isScalar;
		if (dimensions.length == 0) {
			// Scalar dataset
			data = Array.newInstance(javaType, 1);
			isScalar = true;
			dimensions = new int[] { 1 }; // Fake the dimensions
		} else {
			data = Array.newInstance(javaType, dimensions);
			isScalar = false;
		}

		final Map<Long, GlobalHeap> heaps = new HashMap<>();

		List<ByteBuffer> elements = new ArrayList<>();
		for (GlobalHeapId globalHeapId : getGlobalHeapIds(buffer, type.getSize(), hdfFc, getTotalPoints(dimensions))) {
			GlobalHeap heap = heaps.computeIfAbsent(globalHeapId.getHeapAddress(),
					address -> new GlobalHeap(hdfFc, address));

			ByteBuffer bb = heap.getObjectData(globalHeapId.getIndex());
			elements.add(bb);
		}

		// Make the output array
		if(type.isVariableLengthString()) {
			fillStringData(type, data, dimensions, elements.iterator());
		} else {
			fillData(type.getParent(), data, dimensions, elements.iterator());
		}

		if (isScalar) {
			return Array.get(data, 0);
		} else {
			return data;
		}
	}

	private static void fillData(DataType dataType, Object data, int[] dims, Iterator<ByteBuffer> elements) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillData(dataType, newArray, stripLeadingIndex(dims), elements);
			}
		} else {
			for (int i = 0; i < dims[0]; i++) {
				ByteBuffer buffer = elements.next();
				int[] elementDims = new int[]{ buffer.limit() / dataType.getSize()};
				Object elementData = DatasetReader.readDataset(dataType, buffer, elementDims);
				Array.set(data, i, elementData);
			}
		}
	}

	private static void fillStringData(VariableLength dataType, Object data, int[] dims, Iterator<ByteBuffer> elements) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				fillStringData(dataType, newArray, stripLeadingIndex(dims), elements);
			}
		} else {
			for (int i = 0; i < dims[0]; i++) {
				ByteBuffer buffer = elements.next();
				String element = dataType.getEncoding().decode(buffer).toString();
				Array.set(data, i, element);
			}
		}
	}

	private static int[] stripLeadingIndex(int[] dims) {
		return Arrays.copyOfRange(dims, 1, dims.length);
	}

	private static List<GlobalHeapId> getGlobalHeapIds(ByteBuffer bb, int length, HdfFileChannel hdfFc,
			int datasetTotalSize) {
		// For variable length datasets the actual data is in the global heap so need to
		// resolve that then build the buffer.
		List<GlobalHeapId> ids = new ArrayList<>(datasetTotalSize);

		final int skipBytes = length - hdfFc.getSizeOfOffsets() - 4; // id=4

		// Assume all global heap buffers are little endian
		bb.order(LITTLE_ENDIAN);

		while (bb.remaining() >= length) {
			// Move past the skipped bytes. TODO figure out what this is for
			bb.position(bb.position() + skipBytes);
			long heapAddress = Utils.readBytesAsUnsignedLong(bb, hdfFc.getSizeOfOffsets());
			int index = Utils.readBytesAsUnsignedInt(bb, 4);
			GlobalHeapId globalHeapId = new GlobalHeapId(heapAddress, index);
			ids.add(globalHeapId);
		}

		return ids;
	}

	private static int getTotalPoints(int[] dimensions) {
		return IntStream.of(dimensions)
				.reduce(1, Math::multiplyExact);
	}
}
