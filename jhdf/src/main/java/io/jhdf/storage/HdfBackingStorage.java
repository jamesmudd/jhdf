package io.jhdf.storage;

import io.jhdf.Superblock;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
}
