/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.exceptions;

/**
 * Exception to indicate and invalid checksum has been detected. Might indicate possible file corruption.
 *
 * @author James Mudd
 */
public class HdfChecksumMismatchException extends HdfException {

	public HdfChecksumMismatchException(int storedChecksum, int calculatedChecksum) {
		super("Checksum mismatch, possible file corruption. stored checksum = [" + storedChecksum + "] != calculated checksum = [" + calculatedChecksum + "]");
	}
}
