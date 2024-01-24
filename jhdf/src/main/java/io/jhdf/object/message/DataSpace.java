/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.BufferBuilder;
import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.stream.IntStream;

public class DataSpace {

	private static final int MAX_SIZES_PRESENT_BIT = 0;
	private final byte version;
	private final boolean maxSizesPresent;
	private final int[] dimensions;
	private final long[] maxSizes;
	private final byte type; // TODO enum SCALAR SIMPLE NULL

	private DataSpace(ByteBuffer bb, Superblock sb) {

		version = bb.get();
		int numberOfDimensions = bb.get();
		byte[] flagBits = new byte[1];
		bb.get(flagBits);
		BitSet flags = BitSet.valueOf(flagBits);
		maxSizesPresent = flags.get(MAX_SIZES_PRESENT_BIT);

		if (version == 1) {
			// Skip 5 reserved bytes
			bb.position(bb.position() + 5);
			type = -1;
		} else if (version == 2) {
			type = bb.get();
		} else {
			throw new HdfException("Unrecognized version = " + version);
		}

		// Dimensions sizes
		if (numberOfDimensions != 0) {
			dimensions = new int[numberOfDimensions];
			for (int i = 0; i < numberOfDimensions; i++) {
				dimensions[i] = Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfLengths());
			}
		} else {
			dimensions = new int[0];
		}

		// Max dimension sizes
		if (maxSizesPresent) {
			maxSizes = new long[numberOfDimensions];
			for (int i = 0; i < numberOfDimensions; i++) {
				maxSizes[i] = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			}
		} else {
			maxSizes = ArrayUtils.EMPTY_LONG_ARRAY;
		}

		// Permutation indices - Note never implemented in HDF library!
	}

	private DataSpace(byte version, boolean maxSizesPresent, int[] dimensions, long[] maxSizes, byte type, long totalLength) {
		this.version = version;
		this.maxSizesPresent = maxSizesPresent;
		this.dimensions = dimensions;
		this.maxSizes = maxSizes;
		this.type = type;
	}

	public static DataSpace readDataSpace(ByteBuffer bb, Superblock sb) {
		return new DataSpace(bb, sb);
	}

	public static DataSpace fromObject(Object data) {
		return new DataSpace((byte) 2,
			false,
			Utils.getDimensions(data),
			ArrayUtils.EMPTY_LONG_ARRAY,
			(byte) 1,
			0L);
	}

	/**
	 * Gets the total number of elements in this dataspace.
	 *
	 * @return the total number of elements in this dataspace
	 */
	public long getTotalLength() {
		// If type == 2 then it's an empty dataset and totalLength should be 0
		if (type == 2) {
			return  0;
		} else {
			// Calculate the total length by multiplying all dimensions
			return IntStream.of(dimensions)
				.mapToLong(Long::valueOf) // Convert to long to avoid int overflow
				.reduce(1, Math::multiplyExact);
		}
	}

	public int getType() {
		return type;
	}

	public int getVersion() {
		return version;
	}

	public int[] getDimensions() {
		return ArrayUtils.clone(dimensions);
	}

	public long [] getMaxSizes() {
		return ArrayUtils.clone(maxSizes);
	}

	public boolean isMaxSizesPresent() {
		return maxSizesPresent;
	}

	public ByteBuffer toBuffer() {
		BitSet flags = new BitSet(8);
		flags.set(MAX_SIZES_PRESENT_BIT, maxSizesPresent);
		BufferBuilder bufferBuilder = new BufferBuilder()
			.writeByte(version) // Version
			.writeByte(dimensions.length) // no dims
			.writeBitSet(flags, 1)
			.writeByte(type);

        for (int dimension : dimensions) {
			// TODO should be size of length
            bufferBuilder.writeLong(dimension);
        }

		return bufferBuilder.build();
	}
}
