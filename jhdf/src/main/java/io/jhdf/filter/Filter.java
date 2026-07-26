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

import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.exceptions.UnsupportedHdfException;

/**
 * Interface to be implemented to be a HDF5 filter.
 *
 * @author James Mudd
 */
public interface Filter {

	/**
	 * Gets the ID of this filter, this must match the ID in the dataset header.
	 *
	 * @return the ID of this filter
	 */
	int getId();

	/**
	 * Gets the name of this filter e.g. 'deflate', 'shuffle'
	 *
	 * @return the name of this filter
	 */
	String getName();

	/**
	 * Applies this filter to decode data. If the decode fails a
	 * {@link HdfFilterException} will be thrown. This method must be thread safe,
	 * multiple thread may use the filter simultaneously.
	 *
	 * @param encodedData the data to be decoded
	 * @param filterData  the settings from the file this filter was used with. e.g.
	 *                    compression level.
	 * @return the decoded data
	 * @throws HdfFilterException if the decode operation fails
	 */
	byte[] decode(byte[] encodedData, int[] filterData);

	/**
	 * Applies this filter to encode data when writing. The default implementation
	 * throws {@link UnsupportedHdfException}, filters supporting writing must
	 * override this method. This method must be thread safe, multiple threads may
	 * use the filter simultaneously.
	 *
	 * @param data       the data to be encoded
	 * @param filterData the settings this filter is used with. e.g. compression
	 *                   level.
	 * @return the encoded data
	 * @throws HdfFilterException if the encode operation fails
	 * @since v0.13.0
	 */
	default byte[] encode(byte[] data, int[] filterData) {
		throw new UnsupportedHdfException("Filter [" + getName() + " id=" + getId() + "] does not support encoding");
	}

}
