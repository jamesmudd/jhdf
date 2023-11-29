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

		return encodedData;
	}
}
