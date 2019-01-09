package io.jhdf;

import java.nio.channels.FileChannel;
import java.util.Map;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.AttributeMessage;

public class DatasetImpl extends AbstractNode implements Dataset {

	private final LazyInitializer<Map<String, AttributeMessage>> attributes;
	private final LazyInitializer<ObjectHeader> header;

	public DatasetImpl(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		super(address, name, parent);

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
}
