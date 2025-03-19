/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritableNode;
import io.jhdf.api.WritableDataset;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.AttributeInfoMessage;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.GroupInfoMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfFileChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WritableGroupImpl extends AbstractWritableNode implements WritableGroup {

	private static final Logger logger = LoggerFactory.getLogger(WritableGroupImpl.class);

	private final Map<String, WritableNode> children = new ConcurrentHashMap<>();

	public WritableGroupImpl(Group parent, String name) {
		super(parent, name);
	}

	@Override
	public Map<String, Node> getChildren() {
		return Collections.unmodifiableMap(children);
	}

	@Override
	public Node getChild(String name) {
		return children.get(name);
	}

	@Override
	public Node getByPath(String path) {
		throw new UnsupportedHdfException("Not supported by writable groups");
	}

	@Override
	public Dataset getDatasetByPath(String path) {
		throw new UnsupportedHdfException("Not supported by writable groups");
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		return false;
	}

	@Override
	public NodeType getType() {
		return NodeType.GROUP;
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public long getAddress() {
		return 0;
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return false;
	}

	@Override
	public WritableDataset putDataset(String name, Object data) {
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		WritableDatasetImpl writableDataset = new WritableDatasetImpl(data, name, this);
		children.put(name, writableDataset);
		logger.info("Added dataset [{}] to group [{}]", name, getPath());
		return writableDataset;
	}

	@Override
	public WritableDataset putWritableDataset(String name, WritableDataset dsb) {
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		children.put(name, dsb);
		logger.info("Added dataset [{}] to group [{}]", name, getPath());
		return dsb;
	}


	@Override
	public WritableGroup putGroup(String name) {
		if(StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		WritableGroupImpl newGroup = new WritableGroupImpl(this, name);
		children.put(name, newGroup);
		logger.info("Added group [{}] to group [{}]", name, getPath());
		return newGroup;
	}



	@Override
	public Iterator<Node> iterator() {
		Collection<Node> values = Collections.unmodifiableCollection(children.values());
		return values.iterator();
	}

	public long write(HdfFileChannel hdfFileChannel, long position) {
		logger.info("Writing group [{}] at position [{}]", getPath(), position);

		List<Message> messages = new ArrayList<>();
		GroupInfoMessage groupInfoMessage = GroupInfoMessage.createBasic();
		messages.add(groupInfoMessage);
		LinkInfoMessage linkInfoMessage = LinkInfoMessage.createBasic();
		messages.add(linkInfoMessage);

		for (Map.Entry<String, WritableNode> child : children.entrySet()) {
			LinkMessage linkMessage = LinkMessage.create(child.getKey(), 0L);
			messages.add(linkMessage);
		}

		if(!getAttributes().isEmpty()) {
			// Need an attribute info message to allow HDFView to see the attributes
			AttributeInfoMessage attributeInfoMessage = AttributeInfoMessage.create();
			messages.add(attributeInfoMessage);
			for (Map.Entry<String, Attribute> attribute : getAttributes().entrySet()) {
				logger.info("Writing attribute [{}] in group [{}]", attribute.getKey(), getName());
				AttributeMessage attributeMessage = AttributeMessage.create(attribute.getKey(), attribute.getValue());
				messages.add(attributeMessage);
			}
		}

		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);

		ByteBuffer tempBuffer = objectHeader.toBuffer();
		int objectHeaderSize = tempBuffer.limit();
		// Upto here just finding out the size of the OH can be much improved

		// Start building another OH
		messages = new ArrayList<>();
		messages.add(groupInfoMessage);
		messages.add(linkInfoMessage);

		if(!getAttributes().isEmpty()) {
			AttributeInfoMessage attributeInfoMessage = AttributeInfoMessage.create();
			messages.add(attributeInfoMessage);
			for (Map.Entry<String, Attribute> attribute : getAttributes().entrySet()) {
				logger.info("Writing attribute [{}]", attribute.getKey());
				AttributeMessage attributeMessage = AttributeMessage.create(attribute.getKey(), attribute.getValue());
				messages.add(attributeMessage);
			}
		}

		long nextChildAddress = position + objectHeaderSize;

		for (Map.Entry<String, WritableNode> child : children.entrySet()) {
			LinkMessage linkMessage = LinkMessage.create(child.getKey(), nextChildAddress);
			messages.add(linkMessage);
			long endPosition = child.getValue().write(hdfFileChannel, nextChildAddress);
			nextChildAddress = endPosition;
		}

		objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
		hdfFileChannel.write(objectHeader.toBuffer(), position);

		logger.info("Finished writing group [{}]", getPath());
		return nextChildAddress;
	}
}
