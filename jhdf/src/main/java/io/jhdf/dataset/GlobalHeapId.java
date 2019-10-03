/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

public class GlobalHeapId {

	private final long heapAddress;
	private final int index;

	public GlobalHeapId(long heapAddress, int index) {
		this.heapAddress = heapAddress;
		this.index = index;
	}

	public long getHeapAddress() {
		return heapAddress;
	}

	public int getIndex() {
		return index;
	}

}
