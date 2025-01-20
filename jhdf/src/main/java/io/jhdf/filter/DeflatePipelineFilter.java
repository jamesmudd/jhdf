/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class DeflatePipelineFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(DeflatePipelineFilter.class);

	@Override
	public int getId() {
		return 1;
	}

	@Override
	public String getName() {
		return "deflate";
	}

	@Override
	public byte[] decode(byte[] compressedData, int[] filterData) {
		final Inflater inflater = new Inflater();

		// Make a guess that the decompressed data is 3 times larger than compressed.
		// This is a performance optimisation to avoid resizing of the stream byte
		// array.
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(compressedData.length * 3)) {
			// Setup the inflater
			inflater.setInput(compressedData);
			final byte[] buffer = new byte[4096];

			// Do the decompression
			while (!inflater.finished()) {
				int read = inflater.inflate(buffer);
				if(read == 0) {
					throw new HdfFilterException("Zero bytes inflated");
				}
				baos.write(buffer, 0, read);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Decompressed chunk. Compressed size = {} bytes, Decompressed size = {}",
					inflater.getBytesRead(),
					inflater.getBytesWritten());
			}

			return baos.toByteArray();

		} catch (DataFormatException | IOException e) {
			throw new HdfFilterException("Inflating failed", e);
		} finally {
			// Close the inflater
			inflater.end();
		}
	}

	public byte[] encode(byte[] data, int[] filterData) {

		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length)) {
			// Setup the deflater
			DeflaterOutputStream daos = new DeflaterOutputStream(baos,new Deflater(filterData[0]));
		
			// Do the compression
			daos.write(data);
			daos.close();

			byte[] output = baos.toByteArray();

			if (logger.isDebugEnabled()) {
				logger.debug("Decompressed chunk. Decompressed size = {} bytes, Compressed size = {}",
					data.length, output.length);
			}

			return output;

		} catch (IOException e) {
			throw new HdfFilterException("Inflating failed", e);
		} 
	}
}
