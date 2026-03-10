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

import io.jhdf.Superblock;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSpaceTest {

	@Test
	void supportsDimensionsAboveIntegerMaxValue() {
		long largeDimension = (long) Integer.MAX_VALUE + 42;

		DataSpace dataSpace = readDataSpace(new long[] {largeDimension}, null);

		assertThat(dataSpace.getTotalLength()).isEqualTo(largeDimension);
		assertThat(dataSpace.getDimensionsAsLong()).containsExactly(largeDimension);
		assertThat(dataSpace.getMaxSizes()).containsExactly(largeDimension);
		assertThatThrownBy(dataSpace::getDimensions)
			.isInstanceOf(ArithmeticException.class);
	}

	@Test
	void readsExplicitMaxSizesAsLongValues() {
		long maxSize = (long) Integer.MAX_VALUE + 100;

		DataSpace dataSpace = readDataSpace(new long[] {5}, new long[] {maxSize});

		assertThat(dataSpace.getDimensions()).containsExactly(5);
		assertThat(dataSpace.getMaxSizes()).containsExactly(maxSize);
	}

	@Test
	void modifyDimensionsAcceptsLongValues() {
		long largeDimension = (long) Integer.MAX_VALUE + 7;

		DataSpace original = readDataSpace(new long[] {2, 3}, null);
		DataSpace resized = DataSpace.modifyDimensions(original, new long[] {largeDimension, 3});

		assertThat(resized.getTotalLength()).isEqualTo(largeDimension * 3);
		assertThat(resized.getMaxSizes()).containsExactly(largeDimension, 3);
		assertThatThrownBy(resized::getDimensions)
			.isInstanceOf(ArithmeticException.class);
	}

	@Test
	@SuppressWarnings("deprecation")
	void modifyDimensionsAcceptsIntValuesForCompatibility() {
		DataSpace original = readDataSpace(new long[] {2, 3}, null);
		DataSpace resized = DataSpace.modifyDimensions(original, new int[] {4, 5});

		assertThat(resized.getDimensions()).containsExactly(4, 5);
		assertThat(resized.getDimensionsAsLong()).containsExactly(4L, 5L);
		assertThat(resized.getMaxSizes()).containsExactly(4L, 5L);
		assertThat(resized.getTotalLength()).isEqualTo(20);
	}

	private static DataSpace readDataSpace(long[] dimensions, long[] maxSizes) {
		boolean maxSizesPresent = maxSizes != null;
		int bufferSize = 4 + dimensions.length * Long.BYTES + (maxSizesPresent ? maxSizes.length * Long.BYTES : 0);
		ByteBuffer bb = ByteBuffer.allocate(bufferSize).order(LITTLE_ENDIAN);

		bb.put((byte) 2); // version
		bb.put((byte) dimensions.length);
		bb.put((byte) (maxSizesPresent ? 1 : 0)); // flags (bit 0 = max sizes present)
		bb.put((byte) 1); // simple dataspace

		for (long dimension : dimensions) {
			bb.putLong(dimension);
		}
		if (maxSizesPresent) {
			for (long maxSize : maxSizes) {
				bb.putLong(maxSize);
			}
		}

		bb.rewind();
		return DataSpace.readDataSpace(bb, new Superblock.SuperblockV2V3());
	}
}
