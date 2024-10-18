/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class TempHdfFile extends HdfFile {
	private static final Logger logger = LoggerFactory.getLogger(TempHdfFile.class);

	private TempHdfFile(Path tempFile) {
		super(tempFile);
	}

	@Override
	public void close() {
		super.close();
		logger.info("Deleting temp file on close [{}]", getFileAsPath().toAbsolutePath());
		boolean deleteSuccess = getFile().delete();
		if (!deleteSuccess) {
			logger.warn("Could not delete temp file [{}]", getFileAsPath().toAbsolutePath());
		}
	}

	public static TempHdfFile fromInputStream(InputStream inputStream) {
		try {
			Path tempFile = Files.createTempFile(null, "-stream.hdf5"); // null random file name
			logger.info("Creating temp file [{}]", tempFile.toAbsolutePath());
			tempFile.toFile().deleteOnExit(); // Auto cleanup in case close() is never called
			Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			logger.debug("Read stream to temp file [{}]", tempFile.toAbsolutePath());
			return new TempHdfFile(tempFile);
		} catch (IOException e) {
			throw new HdfException("Failed to open input stream", e);
		}
	}
}
