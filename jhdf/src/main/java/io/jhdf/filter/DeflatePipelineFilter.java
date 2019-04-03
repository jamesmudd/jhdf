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

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfFilterException;

public class DeflatePipelineFilter implements PipelineFilter {

	private static final Logger logger = LoggerFactory.getLogger(DeflatePipelineFilter.class);

	public DeflatePipelineFilter(int[] data) {
		// No-op - the compression level is passed in but not needed to decompress
	}

	@Override
	public String getName() {
		return "deflate";
	}

	@Override
	public byte[] decode(byte[] compressedData) {
		try {
			final Inflater inflater = new Inflater();
			inflater.setInput(compressedData);

			final byte[] buffer = new byte[4096];
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			while (!inflater.finished()) {
				int read = inflater.inflate(buffer);
				baos.write(buffer, 0, read);
			}

			final byte[] decompressedData = baos.toByteArray();

			logger.info("Decompressed chunk. Compressed size = {} bytes, Decompressed size = {}",
					inflater.getBytesRead(),
					inflater.getBytesWritten());

			return decompressedData;

		} catch (DataFormatException e) {
			throw new HdfFilterException("Inflating failed", e);
		}
	}
}
