/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2021 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.storage;

import io.jhdf.Superblock;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Interface to underlying storage maybe an on disk file or an in memory file.
 *
 * @author James Mudd
 */
public interface HdfBackingStorage {
	ByteBuffer readBufferFromAddress(long address, int length);

	ByteBuffer map(long address, long length);

	ByteBuffer mapNoOffset(long address, long length);

	long getUserBlockSize();

	Superblock getSuperblock();

	FileChannel getFileChannel();

	int getSizeOfOffsets();

	int getSizeOfLengths();

	void close();

	long size();

	/**
	 * @return true if the file is in memory
	 */
	boolean inMemory();
}
