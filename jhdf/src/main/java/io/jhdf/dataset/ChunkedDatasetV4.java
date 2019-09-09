package io.jhdf.dataset;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class ChunkedDatasetV4 extends DatasetBase {
    private static final Logger logger = LoggerFactory.getLogger(ChunkedDatasetV4.class);

    private final ChunkedDataLayoutMessageV4 dlm;

    public ChunkedDatasetV4(HdfFileChannel hdfFc, long address, String name, Group parent, ObjectHeader oh) {
        super(hdfFc, address, name, parent, oh);

        dlm = oh.getMessageOfType(ChunkedDataLayoutMessageV4.class);

        logger.debug("Created chunked v4 dataset. Index type {}", dlm.getIndexingType());
    }

    @Override
    public ByteBuffer getDataBuffer() {
        return null;
    }
}
