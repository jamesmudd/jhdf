/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset.chunked.indexing;

import io.jhdf.btree.BTreeV2;
import io.jhdf.btree.record.BTreeDatasetChunkRecord;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;
import io.jhdf.storage.HdfBackingStorage;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * Implements B Tree V2 chunk indexing
 *
 * @author James Mudd
 */
public class BTreeIndex implements ChunkIndex {

	private final BTreeV2<BTreeDatasetChunkRecord> bTreeV2;

	public BTreeIndex(HdfBackingStorage hdfBackingStorage, long address, DatasetInfo datasetInfo) {
		bTreeV2 = new BTreeV2<>(hdfBackingStorage, address, datasetInfo);
	}

	@Override
	public Collection<Chunk> getAllChunks() {
		return bTreeV2.getRecords().stream().map(BTreeDatasetChunkRecord::getChunk).collect(toList());
	}
}
