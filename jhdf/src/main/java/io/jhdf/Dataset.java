package io.jhdf;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.nio.channels.FileChannel;
import java.util.Map;

import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.AttributeMessage;

public class Dataset implements Node {

	private final long address;
	private final String name;
	private final Group parent;
	private final Map<String, AttributeMessage> attributes;

	public Dataset(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		this.address = address;
		this.name = name;
		this.parent = parent;

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
	public boolean isGroup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Node> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return parent.getPath() + name;
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		return attributes;
	}

	@Override
	public String getType() {
		return "Dataset";
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public long getAddress() {
		return address;
	}
}
