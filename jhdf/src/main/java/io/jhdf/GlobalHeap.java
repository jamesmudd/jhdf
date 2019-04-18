/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf;

import static io.jhdf.Utils.createSubBuffer;
import static io.jhdf.Utils.readBytesAsUnsignedInt;
import static io.jhdf.Utils.seekBufferToNextMultipleOfEight;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.jhdf.exceptions.HdfException;

public class GlobalHeap {

	private static final byte[] GLOBAL_HEAP_SIGNATURE = "GCOL".getBytes();

	private final HdfFileChannel hdfFc;
	private final long address;

	private final Map<Integer, GlobalHeapObject> objects = new HashMap<>();

	public GlobalHeap(HdfFileChannel hdfFc, long address) {
		this.hdfFc = hdfFc;
		this.address = address;

		try {
			int headerSize = 4 + 1 + 3 + hdfFc.getSizeOfLengths();

			ByteBuffer bb = hdfFc.readBufferFromAddress(address, headerSize);

			byte[] signitureBytes = new byte[4];
			bb.get(signitureBytes, 0, signitureBytes.length);

			// Verify signature
			if (!Arrays.equals(GLOBAL_HEAP_SIGNATURE, signitureBytes)) {
				throw new HdfException("Global heap signature 'GCOL' not matched, at address " + address);
			}

			// Version Number
			final byte version = bb.get();
			if (version != 1) {
				throw new HdfException("Unsupported global heap version detected. Version: " + version);
			}

			bb.position(8); // Skip past 3 reserved bytes

			int collectionSize = readBytesAsUnsignedInt(bb, hdfFc.getSizeOfLengths());

			// Now start reading the heap into memory
			bb = hdfFc.readBufferFromAddress(address + headerSize, collectionSize);

			while (bb.remaining() > 8) {
				GlobalHeapObject object = new GlobalHeapObject(bb);
				if (object.index == 0) {
					break;
				} else {
					objects.put(object.index, object);
				}
			}

		} catch (Exception e) {
			throw new HdfException("Error reading global heap at address " + address, e);
		}
	}

	@Override
	public String toString() {
		return "GlobalHeap [address=" + address + ", objects=" + objects.size() + "]";
	}

	private class GlobalHeapObject {

		final int index;
		final int referenceCount;
		final ByteBuffer data;

		private GlobalHeapObject(ByteBuffer bb) {

			index = readBytesAsUnsignedInt(bb, 2);
			referenceCount = readBytesAsUnsignedInt(bb, 2);
			bb.position(bb.position() + 4); // Skip 4 reserved bytes
			int size = readBytesAsUnsignedInt(bb, hdfFc.getSizeOfOffsets());
			data = createSubBuffer(bb, size);
			seekBufferToNextMultipleOfEight(bb);
		}
	}

	/**
	 * Gets the data buffer for an object in this global heap.
	 *
	 * @param index the requested object
	 * @return the object data buffer
	 * @throws IllegalArgumentException if the requested index is not in the heap
	 */
	public ByteBuffer getObjectData(int index) {
		if (!objects.containsKey(index)) {
			throw new IllegalArgumentException("Global heap doesn't contain object with index: " + index);
		}

		return objects.get(index).data;
	}

	/**
	 * Gets the number of references to an object in this global heap.
	 *
	 * @param index of the object
	 * @return the number of references to the object
	 * @throws IllegalArgumentException if the requested index is not in the heap
	 */
	public int getObjectReferenceCount(int index) {
		if (!objects.containsKey(index)) {
			throw new IllegalArgumentException("Global heap doesn't contain object with index: " + index);
		}

		return objects.get(index).referenceCount;
	}

}
