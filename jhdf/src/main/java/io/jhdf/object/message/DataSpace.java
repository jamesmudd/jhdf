package io.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.stream.LongStream;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.HdfException;

public class DataSpace {

	private final byte version;
	private final boolean maxSizesPresent;
	private final long[] dimensions;
	private final long[] maxSizes;
	private final byte type;

	private DataSpace(ByteBuffer bb, Superblock sb) {

		version = bb.get();
		int numberOfdimensions = bb.get();
		byte[] flagBits = new byte[1];
		bb.get(flagBits);
		BitSet flags = BitSet.valueOf(flagBits);
		maxSizesPresent = flags.get(0);

		if (version == 1) {
			// Skip 5 reserved bytes
			bb.position(bb.position() + 5);
			type = -1;
		} else if (version == 2) {
			type = bb.get();
		} else {
			throw new HdfException("Unreconized version = " + version);
		}

		// Dimensions sizes
		if (numberOfdimensions != 0) {
			dimensions = new long[numberOfdimensions];
			for (int i = 0; i < numberOfdimensions; i++) {
				dimensions[i] = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			}
		} else {
			dimensions = new long[0];
		}

		// Max dimension sizes
		if (maxSizesPresent) {
			maxSizes = new long[numberOfdimensions];
			for (int i = 0; i < numberOfdimensions; i++) {
				maxSizes[i] = Utils.readBytesAsUnsignedLong(bb, sb.getSizeOfLengths());
			}
		} else {
			maxSizes = new long[0];
		}

		// Permutation indices - Note never implemented in HDF library!
	}

	public static DataSpace readDataSpace(ByteBuffer bb, Superblock sb) {
		return new DataSpace(bb, sb);
	}

	/**
	 * Gets the total number of elements in this dataspace.
	 * 
	 * @return the total number of elements in this dataspace
	 * @throws ArithmeticException if an integer overflow occurs
	 */
	public long getTotalLentgh() {
		return LongStream.of(dimensions).reduce(1, Math::multiplyExact);
	}

	public int getType() {
		return type;
	}

	public int getVersion() {
		return version;
	}

	public long[] getDimensions() {
		return dimensions;
	}

	public long[] getMaxSizes() {
		return maxSizes;
	}

	public boolean isMaxSizesPresent() {
		return maxSizesPresent;
	}

}
