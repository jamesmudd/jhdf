package com.jamesmudd.jhdf.object.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.Utils;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class DataSpace {

	private final byte version;
	private final boolean maxSizesPresent;
	private final List<Integer> dimensions;
	private final List<Integer> maxSizes;
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
			dimensions = new ArrayList<>(numberOfdimensions);
			for (int i = 0; i < numberOfdimensions; i++) {
				dimensions.add(Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfLengths()));
			}
		} else {
			dimensions = Collections.emptyList();
		}

		// Max dimension sizes
		if (maxSizesPresent) {
			maxSizes = new ArrayList<>(numberOfdimensions);
			for (int i = 0; i < numberOfdimensions; i++) {
				maxSizes.add(Utils.readBytesAsUnsignedInt(bb, sb.getSizeOfLengths()));
			}
		} else {
			maxSizes = Collections.emptyList();
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
	public int getTotalLentgh() {
		if (dimensions.isEmpty()) {
			return 1;
		}
		return dimensions.stream().mapToInt(Integer::intValue).reduce(1, Math::multiplyExact);
	}

	public int getType() {
		return type;
	}

	public int getVersion() {
		return version;
	}

}
