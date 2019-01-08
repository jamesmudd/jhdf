package io.jhdf;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.nio.channels.FileChannel;
import java.util.Map;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.AttributeMessage;

public class DatasetImpl extends AbstractNode implements Dataset {

	private final Map<String, AttributeMessage> attributes;

	public DatasetImpl(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		super(address, name, parent);

		try {
			ObjectHeader header = ObjectHeader.readObjectHeader(fc, sb, address);

			// Attributes
			attributes = header.getMessagesOfType(AttributeMessage.class).stream()
					.collect(toMap(AttributeMessage::getName, identity()));
		} catch (Exception e) {
			throw new HdfException("Error reading dataset '" + getPath() + "' at address " + address, e);
		}

	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	public Map<String, AttributeMessage> getAttributes() {
		return attributes;
	}
}
