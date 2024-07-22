/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Thrown when a path inside a HDF5 file is invalid. It may contain invalid
 * characters or not be found in the file.
 *
 * @author James Mudd
 */
public class HdfInvalidPathException extends HdfException {

	private static final long serialVersionUID = 1L;

	private final String path;
	private final transient Path file;

	public HdfInvalidPathException(String path, Path file) {
		super("The path '" + path + "' could not be found in the HDF5 file '" + file.toAbsolutePath() + "'");
		this.path = path;
		this.file = file;
	}

	public String getPath() {
		return path;
	}

	public File getFile() {
		return file.getFileSystem() == FileSystems.getDefault() ? file.toFile() : null;
	}

	public Path getFileAsPath() {
		return file;
	}
}
