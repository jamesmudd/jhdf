/*******************************************************************************
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 * 
 * http://jhdf.io
 * 
 * Copyright 2019 James Mudd
 * 
 * MIT License see 'LICENSE' file
 ******************************************************************************/
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;

public interface PipelineFilter {

	/**
	 * Gets the name of this filter e.g. 'deflate', 'shuffle'
	 * 
	 * @return the name of this filter
	 */
	String getName();

	/**
	 * Applies this filter to decode data
	 * 
	 * @param encodedData the data to be decoded
	 * @return the decoded data
	 * @throws HdfFilterException if the decode operation fails
	 */
	byte[] decode(byte[] encodedData) throws HdfFilterException;

}
