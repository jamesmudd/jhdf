/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.datatype;

import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for reading compound data type messages.
 *
 * @author James Mudd
 */
public class CompoundDataType extends DataType {

	private final List<CompoundDataMember> members;

	public CompoundDataType(ByteBuffer bb) {
		super(bb);

		final int version = getVersion();
		if(version > 3) {
			throw new UnsupportedHdfException("Compound data type version is not yet supported: version = " + version);
		}

		int numberOfMembers = Utils.bitsToInt(classBits, 0, 16);
		members = new ArrayList<>(numberOfMembers);

		for (int i = 0; i < numberOfMembers; i++) {

			// The name is null padded to 8 bytes so need to work that out
			final int posBeforeName = bb.position();
			final String name = Utils.readUntilNull(bb);

			// <3 it is null padded, v3 is NOT padded
			if (version < 3) {
				final int posAfterName = bb.position();
				final int bytesPastEight = (posAfterName - posBeforeName) % 8;
				if(bytesPastEight != 0) {
					int bytesToSkip = 8 - bytesPastEight;
					bb.position(bb.position() + bytesToSkip);
				}
			}

			final int offset;
			if(version < 3)
				offset = Utils.readBytesAsUnsignedInt(bb, 4);
			else {
				int offsetBytes = Utils.bytesNeededToHoldNumber(getSize());
				offset = Utils.readBytesAsUnsignedInt(bb, offsetBytes);
			}

			// Think this dimension size is pointless but its the spec...
			int[] dimensionSize = null;
			if (version == 1) {
				final int dimensionality = Utils.readBytesAsUnsignedInt(bb, 1);
				// Skip 3 reserved bytes + 4 not implemented permutation bytes + 4 more reserved bytes
				bb.position(bb.position() + 3 + 4 + 4);

				dimensionSize = new int[dimensionality];
				for (int j = 0; j < 4; j++) {
					int dimSize = Utils.readBytesAsUnsignedInt(bb, 4);
					if (j < dimensionality) {
						dimensionSize[j] = dimSize;
					}
				}
			}

			final DataType memberDataType = DataType.readDataType(bb);

			members.add(new CompoundDataMember(name, dimensionSize, offset, memberDataType));
		}
	}

	@Override
	public Class<?> getJavaType() {
		return Map.class;
	}

	public List<CompoundDataMember> getMembers() {
		return members;
	}

	public static class CompoundDataMember {
		private final String name;
		private final int[] dimensionSize;
		private final int offset;
		private final DataType dataType;

		private CompoundDataMember(String name, int[] dimensionSize, int offset, DataType dataType) {
			this.name = name;
			this.dimensionSize = dimensionSize;
			this.offset = offset;
			this.dataType = dataType;
		}

		public String getName() {
			return name;
		}

		public int[] getDimensionSize() {
			return dimensionSize;
		}

		public int getOffset() {
			return offset;
		}

		public DataType getDataType() {
			return dataType;
		}
	}
}
