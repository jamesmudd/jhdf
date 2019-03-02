package io.jhdf.dataset;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import io.jhdf.ObjectHeader;
import io.jhdf.Superblock;
import io.jhdf.api.Group;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ContigiousDataLayoutMessage;

public class ContigiousDataset extends DatasetBase {

	public ContigiousDataset(FileChannel fc, Superblock sb, long address, String name, Group parent, ObjectHeader oh) {
		super(fc, sb, address, name, parent, oh);
	}

	@Override
	public ByteBuffer getDataBuffer() {
		ContigiousDataLayoutMessage contigiousDataLayoutMessage = getHeaderMessage(ContigiousDataLayoutMessage.class);
		try {
			ByteBuffer data = fc.map(MapMode.READ_ONLY, contigiousDataLayoutMessage.getAddress(),
					contigiousDataLayoutMessage.getSize());
			convertToCorrectEndiness(data);
			return data;
		} catch (Exception e) {
			throw new HdfException("Failed to map data buffer for dataset '" + getPath() + "'", e);
		}
	}

}
