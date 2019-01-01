package io.jhdf;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.SymbolTableMessage;

public class GroupImpl implements Group {

	private final String name;
	private final long address;
	private final Group parent;
	private final Map<String, Node> children;
	private final Map<String, AttributeMessage> attributes;

	private final BTreeNode rootbTreeNode;
	private final LocalHeap rootNameHeap;

	private GroupImpl(FileChannel fc, Superblock sb, long bTreeAddress, long nameHeapAddress, long ojbectHeaderAddress,
			String name, Group parent) {
		this.name = name;
		this.address = ojbectHeaderAddress;
		this.parent = parent;

		rootbTreeNode = new BTreeNode(fc, bTreeAddress, sb);
		rootNameHeap = new LocalHeap(fc, nameHeapAddress, sb);

		final ByteBuffer nameBuffer = rootNameHeap.getDataBuffer();

		children = new LinkedHashMap<>(rootbTreeNode.getEntriesUsed());

		for (long child : rootbTreeNode.getChildAddresses()) {
			GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(fc, child, sb);
			for (SymbolTableEntry e : groupSTE.getSymbolTableEntries()) {
				String childName = readName(nameBuffer, e.getLinkNameOffset());
				if (e.getCacheType() == 1) { // Its a group
					Group group = createGroup(fc, sb, e.getObjectHeaderAddress(), childName, this);
					children.put(childName, group);
				} else { // Dataset
					Dataset dataset = new Dataset(childName, this);
					children.put(childName, dataset);
				}
			}
		}

		// Add attributes
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, ojbectHeaderAddress);
		attributes = oh.getMessagesOfType(AttributeMessage.class).stream()
				.collect(toMap(AttributeMessage::getName, identity()));
	}

	private GroupImpl(FileChannel fc, Superblock sb, ObjectHeader oh, String name, Group parent) {
		this.name = name;
		this.address = oh.getAddress();
		this.parent = parent;

		LinkInfoMessage linkInfoMessage = oh.getMessagesOfType(LinkInfoMessage.class).get(0);

		if (linkInfoMessage.getbTreeNameIndexAddress() == Constants.UNDEFINED_ADDRESS) {
			rootbTreeNode = null;
			rootNameHeap = null;

			List<LinkMessage> links = oh.getMessagesOfType(LinkMessage.class);
			children = new LinkedHashMap<>(links.size());
			for (LinkMessage link : links) {
				ObjectHeader linkHeader = ObjectHeader.readObjectHeader(fc, sb, link.getHardLinkAddress());
				if (!linkHeader.getMessagesOfType(DataSpaceMessage.class).isEmpty()) {
					// Its a a Dataset
					Dataset dataset = new Dataset(link.getLinkName(), this);
					children.put(link.getLinkName(), dataset);
				} else {
					// Its a group
					GroupImpl group = createGroup(fc, sb, link.getHardLinkAddress(), link.getLinkName(), this);
					children.put(link.getLinkName(), group);
				}

			}

		} else {
			throw new UnsupportedHdfException("Only compact link storage is supported");
		}

		// Add attributes
		attributes = oh.getMessagesOfType(AttributeMessage.class).stream()
				.collect(toMap(AttributeMessage::getName, identity()));

	}

	private String readName(ByteBuffer bb, int linkNameOffset) {
		bb.position(linkNameOffset);
		return Utils.readUntilNull(bb);
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public Map<String, Node> getChildren() {
		return children;
	}

	/* package */ static GroupImpl createGroup(FileChannel fc, Superblock sb, long objectHeaderAddress, String name,
			Group parent) {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, objectHeaderAddress);

		if (oh.hasMessageOfType(SymbolTableMessage.class)) {
			// Its an old style Group
			SymbolTableMessage stm = oh.getMessageOfType(SymbolTableMessage.class);
			return new GroupImpl(fc, sb, stm.getbTreeAddress(), stm.getLocalHeapAddress(), objectHeaderAddress, name,
					parent);
		} else {
			// Its a new style group
			return new GroupImpl(fc, sb, oh, name, parent);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Group [name=" + name + ", path=" + getPath() + ", address=" + Utils.toHex(address) + "]";
	}

	@Override
	public String getPath() {
		return parent.getPath() + name + "/";
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		return attributes;
	}

	@Override
	public String getType() {
		return "Group";
	}

	@Override
	public Node getParent() {
		return parent;
	}

}
