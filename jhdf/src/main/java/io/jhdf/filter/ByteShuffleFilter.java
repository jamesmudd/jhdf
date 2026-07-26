/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

public class ByteShuffleFilter implements Filter {

	public static final int ID = 2;

	@Override
	public int getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "shuffle";
	}

	@Override
	public byte[] decode(byte[] data, int[] filterData) {

		// Bytes in each element e.g float32 = 4 bytes
		final int dataSize = filterData[0];

		// A quick shortcut if no shuffling is needed
		if (dataSize == 1) {
			return data;
		}

		final int elements = data.length / dataSize;

		// shuffle doesn't change the size of the data it rearranges it
		final byte[] out = new byte[data.length];

		int pos = 0;
		for (int i = 0; i < dataSize; i++) {
			for (int j = 0; j < elements; j++) {
				out[j * dataSize + i] = data[pos];
				pos++; // step through the input array
			}
		}

		// Very special case here for mismatched length e.g use deflate then shuffle
		// Not sure if there is actually any good use case
		if(pos < data.length) {
			// In the overrun section no shuffle is done just a straight copy
			System.arraycopy(data, pos, out, pos, data.length - pos);
		}

		return out;
	}

	@Override
	public byte[] encode(byte[] data, int[] filterData) {

		// Bytes in each element e.g float32 = 4 bytes
		final int dataSize = filterData[0];

		// A quick shortcut if no shuffling is needed
		if (dataSize <= 1) {
			return data;
		}

		final int elements = data.length / dataSize;

		// shuffle doesn't change the size of the data it rearranges it
		final byte[] out = new byte[data.length];

		// Inverse of decode, gathers the i'th byte of every element together
		int pos = 0;
		for (int i = 0; i < dataSize; i++) {
			for (int j = 0; j < elements; j++) {
				out[pos] = data[j * dataSize + i];
				pos++; // step through the output array
			}
		}

		// If the data is not a whole number of elements copy the remaining bytes unshuffled
		if (pos < data.length) {
			System.arraycopy(data, pos, out, pos, data.length - pos);
		}

		return out;
	}
}
