package io.jhdf.api;

import io.jhdf.storage.HdfFileChannel;

public interface WritableNode extends Node {

	long write(HdfFileChannel hdfFileChannel, long position);
}
