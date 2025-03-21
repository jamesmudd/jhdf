/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
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
import java.util.Arrays;
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
			maxSizes = Arrays.stream(dimensions).asLongStream().toArray();
		}

		// Permutation indices - Note never implemented in HDF library!
	}

	private DataSpace(byte version, boolean maxSizesPresent, int[] dimensions, long[] maxSizes, byte type) {
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
		if(data.getClass().isArray()) {
			int[] dimensions1 = Utils.getDimensions(data);
			return new DataSpace((byte) 2,
				false,
				dimensions1,
				Arrays.stream(dimensions1).asLongStream().toArray(),
				(byte) 1 // Simple
			);
		} else {
			// Scalar
			return new DataSpace((byte) 2,
				false,
				new int[] {},
				new long[] {},
				(byte) 0); // Scalar
		}
		// TODO null/empty datasets
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

	/**
	 When derived dataspaces from multiple chunks of data from the dataset
	 are presented, they need to be validated as consistent for the dataset, or enhanced to
	 support the superset of the two.
	 @param ds2
	 the other, presumably compatible data space
	 @return a combined data space
	 */
	public DataSpace combineDim0(DataSpace ds2) {
		if (this.type != ds2.type) {
			throw new HdfException("Can't combine data spaces of different types");
		}
		if (this.dimensions.length != ds2.dimensions.length) {
			throw new HdfException("Can't combine data spaces of incongruent dimensional structure");
		}

		int[] dims = Arrays.copyOf(this.dimensions, this.dimensions.length);
		dims[0] = Math.addExact(this.dimensions[0], ds2.dimensions[0]);
		if (dims.length > 1) {
			for (int i = 1; i < dims.length; i++) {
				if (this.dimensions[i] != ds2.dimensions[i]) {
					throw new HdfException(
							"Can't combine data spaces of incongruent dimensionality at dimension " + i);
				}
			}
		}

		long[] sizes = Arrays.copyOf(this.maxSizes, this.maxSizes.length);
		sizes[0] = Math.addExact(this.maxSizes[0], ds2.maxSizes[0]);

		return new DataSpace(this.version, this.maxSizesPresent, dims, sizes, this.type);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("DataSpace{");
		sb.append("dimensions=");
		if (dimensions == null)
			sb.append("null");
		else {
			sb.append('[');
			for (int i = 0; i < dimensions.length; ++i)
				sb.append(i == 0 ? "" : ", ").append(dimensions[i]);
			sb.append(']');
		}
		sb.append(", version=").append(version);
		sb.append(", maxSizesPresent=").append(maxSizesPresent);
		sb.append(", maxSizes=");
		if (maxSizes == null)
			sb.append("null");
		else {
			sb.append('[');
			for (int i = 0; i < maxSizes.length; ++i)
				sb.append(i == 0 ? "" : ", ").append(maxSizes[i]);
			sb.append(']');
		}
		sb.append(", type=").append(type);
		sb.append('}');
		return sb.toString();
	}

	/**
	 Resize a dataset's dimensions to those provided. Only the number of provided dimensions are
	 altered, the rest are unchanged. For example, if you resize a 3D dataset with dimensions 10x10x10
	 and the size 10x15, then the resulting size is 10x15x10.
	 @param ds
	 the dataset to resize
	 @param dimensions
	 the new dimensions
	 @return the resized dataset
	 */
	public static DataSpace modifyDimensions(DataSpace ds, int[] dimensions) {
		int[] newdims = Arrays.copyOf(ds.dimensions, ds.getDimensions().length);
		System.arraycopy(dimensions, 0, newdims, 0, dimensions.length);
		long[] newsizes = Arrays.copyOf(ds.maxSizes, ds.maxSizes.length);
		for (int i = 0; i < dimensions.length; i++) {
			newsizes[i] = dimensions[i];
		}
		return new DataSpace(ds.version, ds.maxSizesPresent, newdims, newsizes, ds.type);
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof DataSpace))
			return false;
		DataSpace dataSpace = (DataSpace) o;

		return version == dataSpace.version && maxSizesPresent == dataSpace.maxSizesPresent
					 && type == dataSpace.type && Arrays.equals(dimensions, dataSpace.dimensions)
					 && Arrays.equals(maxSizes, dataSpace.maxSizes);
	}

	@Override
	public int hashCode() {
		int result = version;
		result = 31 * result + Boolean.hashCode(maxSizesPresent);
		result = 31 * result + Arrays.hashCode(dimensions);
		result = 31 * result + Arrays.hashCode(maxSizes);
		result = 31 * result + type;
		return result;
	}

}
