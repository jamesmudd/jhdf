package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritableNode;
import io.jhdf.api.WritiableDataset;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.GroupInfoMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfFileChannel;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WritableGroupImpl implements WritableGroup {

	private final Map<String, WritableNode> children = new ConcurrentHashMap<>();

	private final Group parent;
	private final String name; // TODO Node superclass

	public WritableGroupImpl(Group parent, String name) {
		this.parent = parent;
		this.name = name;
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
	public Group getParent() {
		return parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		if (parent == null) {
			return "/" + getName();
		} else {
			return parent.getPath() + "/" + getName();
		}
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public Attribute getAttribute(String name) {
		return null;
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
	public File getFile() {
		return null;
	}

	@Override
	public Path getFileAsPath() {
		return null;
	}

	@Override
	public HdfFile getHdfFile() {
		return null;
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
	public WritiableDataset putDataset(String name, Object data) {
		WritableDatasetImpl writableDataset = new WritableDatasetImpl(data, name, this);
		children.put(name, writableDataset);
		return writableDataset;
	}

	@Override
	public WritableGroup putGroup(String name) {
		WritableGroupImpl newGroup = new WritableGroupImpl(this, name);
		children.put(name, newGroup);
		return newGroup;
	}

	@Override
	public Iterator<Node> iterator() {
		Collection<Node> values = Collections.unmodifiableCollection(children.values());
		return values.iterator();
	}

	public long write(HdfFileChannel hdfFileChannel, long position) {

		List<Message> messages = new ArrayList<>();
		GroupInfoMessage groupInfoMessage = GroupInfoMessage.createBasic();
		messages.add(groupInfoMessage);
		LinkInfoMessage linkInfoMessage = LinkInfoMessage.createBasic();
		messages.add(linkInfoMessage);

		Map<String, Long> childAddresses = new HashMap<>();
		for (Map.Entry<String, WritableNode> child : children.entrySet()) {
			LinkMessage linkMessage = LinkMessage.create(child.getKey(), 0L);
			childAddresses.put(child.getKey(), 0L);
		}

		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);


		ByteBuffer tempBuffer = objectHeader.toBuffer();
		int objectHeaderSize = tempBuffer.limit();
		// Upto here just finding out the size of the OH can be much improved

		// Start building another OH
		messages = new ArrayList<>();
		messages.add(groupInfoMessage);
		messages.add(linkInfoMessage);

		long nextChildAddress = position + objectHeaderSize;

		for (Map.Entry<String, WritableNode> child : children.entrySet()) {
			LinkMessage linkMessage = LinkMessage.create(child.getKey(), nextChildAddress);
			messages.add(linkMessage);
			long bytesWritten = child.getValue().write(hdfFileChannel, nextChildAddress);
			nextChildAddress += bytesWritten;
		}

		objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);

		return hdfFileChannel.write(objectHeader.toBuffer(), position);
	}
}