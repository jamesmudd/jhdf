package io.jhdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.api.Attribute;
import io.jhdf.api.Node;
import io.jhdf.dataset.DatasetReader;
import io.jhdf.object.message.AttributeMessage;

public class AttributeImpl implements Attribute {
	private static final Logger logger = LoggerFactory.getLogger(AttributeImpl.class);

	private final Node node;
	private final String name;
	private final AttributeMessage message;

	public AttributeImpl(Node node, AttributeMessage message) {
		this.node = node;
		this.name = message.getName();
		this.message = message;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public long getSize() {
		return message.getDataSpace().getTotalLentgh();
	}

	@Override
	public long getDiskSize() {
		return getSize() * message.getDataType().getSize();
	}

	@Override
	public int[] getDimensions() {
		return message.getDataSpace().getDimensions();
	}

	@Override
	public Object getData() {
		logger.debug("Getting data for attribute '{}' of '{}'...", name, node.getPath());
		return DatasetReader.readDataset(message.getDataType(), message.getDataBuffer(), getDimensions());
	}

	@Override
	public Class<?> getJavaType() {
		return message.getDataType().getJavaType();
	}

}
