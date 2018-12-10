package com.jamesmudd.jhdf;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jamesmudd.jhdf.object.message.SymbolTableMessage;

public class Group implements Node {

	private final String name;
	private final long address;
	private final Group parent;
	private final Map<String, Node> children;

	private final BTreeNode rootbTreeNode;
	private final LocalHeap rootNameHeap;

	private Group(FileChannel fc, Superblock sb, long bTreeAddress, long nameHeapAddress, long ojbectHeaderAddress,
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
					Group group = createGroup(fc, sb, e.getAddress(), childName, this);
					children.put(childName, group);
				} else { // Dataset
					Dataset dataset = new Dataset(childName, this);
					children.put(childName, dataset);
				}
			}

		}
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

	public static Group createGroup(FileChannel fc, Superblock sb, long steAddress, String name, Group parent) {
		SymbolTableEntry symbolTableEntry = new SymbolTableEntry(fc, steAddress, sb);
		return new Group(fc, sb, symbolTableEntry.getBTreeAddress(), symbolTableEntry.getNameHeapAddress(),
				symbolTableEntry.getObjectHeaderAddress(), name, parent);
	}

	public static Group createGroupFromObjectHeader(FileChannel fc, Superblock sb, long objectHeaderAddress,
			String name, Group parent) {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, objectHeaderAddress);
		SymbolTableMessage stm = oh.getMessages().stream().filter(SymbolTableMessage.class::isInstance)
				.map(SymbolTableMessage.class::cast).findFirst().get();

		return new Group(fc, sb, stm.getbTreeAddress(), stm.getLocalHeapAddress(), objectHeaderAddress, name, parent);
	}

	protected static Group createRootGroup(FileChannel fc, Superblock sb, long steAddress) {
		SymbolTableEntry symbolTableEntry = new SymbolTableEntry(fc, steAddress, sb);
		return new RootGroup(fc, sb, symbolTableEntry.getBTreeAddress(), symbolTableEntry.getNameHeapAddress(),
				symbolTableEntry.getObjectHeaderAddress());
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
		return parent.getPath() + "/" + name;
	}

	/**
	 * Special type of group for the root. Need a fixed defined name and to return
	 * no path.
	 */
	private static class RootGroup extends Group {

		private static final String ROOT_GROUP_NAME = "/";
		private final long address;

		public RootGroup(FileChannel fc, Superblock sb, long bTreeAddress, long nameHeapAddress,
				long ojbectHeaderAddress) {
			super(fc, sb, bTreeAddress, nameHeapAddress, ojbectHeaderAddress, ROOT_GROUP_NAME, null);
			this.address = ojbectHeaderAddress;
		}

		@Override
		public String getPath() {
			return "";
		}

		@Override
		public String toString() {
			return "RootGroup [name=" + ROOT_GROUP_NAME + ", path=/, address=" + Utils.toHex(address) + "]";
		}
	}

}
