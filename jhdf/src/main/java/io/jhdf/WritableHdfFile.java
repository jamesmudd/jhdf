package io.jhdf;

import io.jhdf.exceptions.HdfWritingExcpetion;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class WritableHdfFile implements AutoCloseable {

	private final FileChannel fileChannel;

	public WritableHdfFile(FileChannel fileChannel) {
		this.fileChannel = fileChannel;
		Superblock.SuperblockV2V3 superblock = new Superblock.SuperblockV2V3();
		createRootGroup();
		try {
			fileChannel.write(superblock.toBuffer());
		} catch (IOException e) {
			throw new HdfWritingExcpetion("Failed to write superblock", e);
		}
	}

	private void createRootGroup() {


	}

	@Override
	public void close() {
		try {
			fileChannel.close();
		} catch (IOException e) {
			throw new HdfWritingExcpetion("Failed to close file", e);
		}
	}
}
