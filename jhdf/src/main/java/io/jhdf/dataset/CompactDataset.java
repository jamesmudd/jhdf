package io.jhdf.dataset;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.jhdf.ObjectHeader;
import io.jhdf.Superblock;
import io.jhdf.api.Group;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;

public class CompactDataset extends DatasetBase {

	public CompactDataset(FileChannel fc, Superblock sb, long address, String name, Group parent, ObjectHeader oh) {
		super(fc, sb, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ByteBuffer data = getHeaderMessage(CompactDataLayoutMessage.class).getDataBuffer();
		convertToCorrectEndiness(data);
		return data;
	}

}
