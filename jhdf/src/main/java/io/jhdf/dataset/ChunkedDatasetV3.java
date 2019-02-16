package io.jhdf.dataset;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.lang3.ArrayUtils;

import io.jhdf.ObjectHeader;
import io.jhdf.Superblock;
import io.jhdf.api.Group;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV1.BTreeV1Data;
import io.jhdf.btree.BTreeV1.BTreeV1Data.Chunk;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;

public class ChunkedDatasetV3 extends DatasetBase {

	public ChunkedDatasetV3(FileChannel fc, Superblock sb, long address, String name, Group parent, ObjectHeader oh) {
		super(fc, sb, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ChunkedDataLayoutMessageV3 chunkedLayout = getHeaderMessage(ChunkedDataLayoutMessageV3.class);
		BTreeV1Data bTree = BTreeV1.createDataBTree(fc, sb, chunkedLayout.getBTreeAddress(),
				getDimensions().length);

		byte[] dataArray = new byte[0];

		for (Chunk chunk : bTree.getChunks()) {
			int size = chunk.getSize();
			ByteBuffer bb = ByteBuffer.allocate(size);
			try {
				fc.read(bb, chunk.getAddress());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dataArray = ArrayUtils.addAll(dataArray, bb.array());
		}
		return ByteBuffer.wrap(dataArray);
	}

}
