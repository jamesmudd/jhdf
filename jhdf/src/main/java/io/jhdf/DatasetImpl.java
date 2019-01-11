package io.jhdf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContigiousDataLayoutMessage;

public class DatasetImpl extends AbstractNode implements Dataset {

	private final LazyInitializer<Map<String, AttributeMessage>> attributes;
	private final LazyInitializer<ObjectHeader> header;
	private final FileChannel fc;

	public DatasetImpl(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		super(address, name, parent);
		this.fc = fc;

		try {
			header = ObjectHeader.lazyReadObjectHeader(fc, sb, address);

			// Attributes
			attributes = new AttributesLazyInitializer(header);
		} catch (Exception e) {
			throw new HdfException("Error reading dataset '" + getPath() + "' at address " + address, e);
		}

	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		try {
			return attributes.get();
		} catch (ConcurrentException e) {
			throw new HdfException(
					"Failed to load attributes for dataset '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public ByteBuffer getDataBuffer() {
		try {
			DataLayoutMessage dataLayoutMessage = header.get().getMessageOfType(DataLayoutMessage.class);
			if (dataLayoutMessage instanceof CompactDataLayoutMessage) {
				return ((CompactDataLayoutMessage) dataLayoutMessage).getDataBuffer();
			} else if (dataLayoutMessage instanceof ContigiousDataLayoutMessage) {
				ContigiousDataLayoutMessage contigiousDataLayoutMessage = (ContigiousDataLayoutMessage) dataLayoutMessage;
				return fc.map(MapMode.READ_ONLY, contigiousDataLayoutMessage.getAddress(),
						contigiousDataLayoutMessage.getSize());
			} else if (dataLayoutMessage instanceof ChunkedDataLayoutMessage) {
				throw new UnsupportedHdfException("Chunked datasets not supported yet");
			} else {
				throw new HdfException(
						"Unsupported data layout. Layout class is: " + dataLayoutMessage.getClass().getCanonicalName());
			}

		} catch (ConcurrentException | IOException e) {
			throw new HdfException(
					"Failed to get data buffer for dataset '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}
}
