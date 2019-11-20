package io.jhdf.dataset.chunked.indexing;

import io.jhdf.HdfFileChannel;
import io.jhdf.btree.BTreeV2;
import io.jhdf.btree.record.BTreeDatasetChunkRecord;
import io.jhdf.btree.record.FilteredDatasetChunks;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.DatasetInfo;

import java.util.Collection;
import java.util.stream.Collectors;

public class BTreeIndex implements ChunkIndex {

    private final BTreeV2<BTreeDatasetChunkRecord> bTreeV2;

    public BTreeIndex(HdfFileChannel hdfFc, long address, DatasetInfo datasetInfo) {
        bTreeV2 = new BTreeV2(hdfFc, address, datasetInfo);
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return bTreeV2.getRecords().stream().map(BTreeDatasetChunkRecord::getChunk).collect(Collectors.toList());
    }
}
