/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.Utils;

import java.util.Arrays;
import java.util.BitSet;

public class ScaleOffsetFilter implements Filter {

	/**
	 * Id defined in https://support.hdfgroup.org/services/filters.html
	 *
	 * @return Defined value, 6
	 */
	@Override
	public int getId() {
		return 6;
	}

	/**
	 * The name of this filter, "scale-offset"
	 *
	 * @return "scale-offset"
	 */
	@Override
	public String getName() {
		return "scale-offset";
	}

	@Override
	public byte[] decode(byte[] encodedData, int[] filterData) {
		int scaleType = filterData[0];
		int minBits = filterData[1];
		int elementCount = filterData[2];
		int elementBytes = filterData[4];
		// TODO fill value?
		// TODO endindness?

		BitSet encodedBits = BitSet.valueOf(encodedData);
		BitSet decodedBits = new BitSet(elementCount * elementBytes * 8);

		int nBits = 3;

		int encodedBitPos = 0;
		int decodedBitPos = 0;
		for (int i = 0; i < elementCount; i++) {
			for (int j = 0; j < nBits; j++) {
				decodedBits.set(decodedBitPos++, false);
			}
			for (int j = 0; j < elementBytes * 8 - nBits; j++) {
				decodedBits.set(decodedBitPos++, encodedBits.get(encodedBitPos++));
			}
		}


		return Arrays.copyOf(decodedBits.toByteArray(), elementCount * elementBytes);
	}
}
